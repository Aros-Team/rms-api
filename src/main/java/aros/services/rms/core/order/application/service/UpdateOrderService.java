/* (C) 2026 */

package aros.services.rms.core.order.application.service;

import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.port.input.InventoryMovementUseCase;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.order.application.dto.TakeOrderCommand;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.UpdateOrderUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.specialselection.application.service.SpecialSelectionPricingService;
import aros.services.rms.core.specialselection.application.service.SpecialSelectionValidator;
import aros.services.rms.core.specialselection.domain.SelectionType;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementation of the use case to update or cancel an existing order, including reverting and
 * re-applying inventory deductions.
 */
public class UpdateOrderService implements UpdateOrderUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(UpdateOrderService.class);
  private final OrderRepositoryPort orderRepositoryPort;
  private final TableRepositoryPort tableRepositoryPort;
  private final ProductRepositoryPort productRepositoryPort;
  private final ProductOptionRepositoryPort productOptionRepositoryPort;
  private final InventoryStockUseCase inventoryStockUseCase;
  private final InventoryMovementUseCase inventoryMovementUseCase;
  private final BusinessMetricsPort metricsPort;
  private final SpecialSelectionRepositoryPort specialSelectionRepositoryPort;
  private final SpecialSelectionValidator specialSelectionValidator;
  private final SpecialSelectionPricingService specialSelectionPricingService;

  /**
   * Creates a new update order service.
   *
   * @param orderRepositoryPort the order repository port
   * @param tableRepositoryPort the table repository port
   * @param productRepositoryPort the product repository port
   * @param productOptionRepositoryPort the product option repository port
   * @param inventoryStockUseCase the inventory stock use case
   * @param inventoryMovementUseCase the inventory movement use case
   * @param metricsPort the business metrics port
   * @param specialSelectionRepositoryPort the special selection repository port
   * @param specialSelectionValidator the special selection validator
   * @param specialSelectionPricingService the special selection pricing service
   */
  public UpdateOrderService(
      OrderRepositoryPort orderRepositoryPort,
      TableRepositoryPort tableRepositoryPort,
      ProductRepositoryPort productRepositoryPort,
      ProductOptionRepositoryPort productOptionRepositoryPort,
      InventoryStockUseCase inventoryStockUseCase,
      InventoryMovementUseCase inventoryMovementUseCase,
      BusinessMetricsPort metricsPort,
      SpecialSelectionRepositoryPort specialSelectionRepositoryPort,
      SpecialSelectionValidator specialSelectionValidator,
      SpecialSelectionPricingService specialSelectionPricingService) {
    this.orderRepositoryPort = orderRepositoryPort;
    this.tableRepositoryPort = tableRepositoryPort;
    this.productRepositoryPort = productRepositoryPort;
    this.productOptionRepositoryPort = productOptionRepositoryPort;
    this.inventoryStockUseCase = inventoryStockUseCase;
    this.inventoryMovementUseCase = inventoryMovementUseCase;
    this.metricsPort = metricsPort;
    this.specialSelectionRepositoryPort = specialSelectionRepositoryPort;
    this.specialSelectionValidator = specialSelectionValidator;
    this.specialSelectionPricingService = specialSelectionPricingService;
  }

  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Order cancel(Long id) {
    Order order =
        orderRepositoryPort
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

    if (order.getStatus() != OrderStatus.QUEUE) {
      throw new IllegalStateException("Order can only be cancelled when in QUEUE status");
    }

    order.setStatus(OrderStatus.CANCELLED);
    Order savedOrder = orderRepositoryPort.save(order);

    if (order.getTable() != null) {
      Table table = order.getTable();
      table.setStatus(TableStatus.AVAILABLE);
      tableRepositoryPort.save(table);
    }

    metricsPort.recordOrderCancellation("user_request");
    return savedOrder;
  }

  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Order update(Long id, TakeOrderCommand command) {
    Order order =
        orderRepositoryPort
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

    if (order.getStatus() != OrderStatus.QUEUE) {
      throw new IllegalStateException("Order can only be updated when in QUEUE status");
    }

    List<OrderDetail> newDetails = new ArrayList<>();
    for (TakeOrderCommand.OrderDetailCommand detailCommand : command.getDetails()) {
      Product product =
          productRepositoryPort
              .findById(detailCommand.getProductId())
              .orElseThrow(() -> new IllegalArgumentException("Product not found"));

      boolean hasSelectedOptions =
          detailCommand.getSelectedOptionIds() != null
              && !detailCommand.getSelectedOptionIds().isEmpty();

      List<ProductOption> selectedOptions = new ArrayList<>();
      if (hasSelectedOptions) {
        selectedOptions =
            productOptionRepositoryPort.findAllById(detailCommand.getSelectedOptionIds());
      }

      double unitPrice = product.getBasePrice() != null ? product.getBasePrice() : 0.0;

      if (product.getSelectionType() == SelectionType.SPECIAL_SELECTION) {
        Optional<SpecialSelectionConfiguration> configOpt =
            specialSelectionRepositoryPort.findById(product.getId());
        if (configOpt.isPresent()) {
          SpecialSelectionConfiguration config = configOpt.get();
          specialSelectionValidator.validateOrderSelections(
              config,
              detailCommand.getSelectedProductIds(),
              detailCommand.getAdditionIds(),
              detailCommand.getClarifications());
          unitPrice =
              specialSelectionPricingService.computeUnitPrice(
                  config, detailCommand.getAdditionIds());
        }
      }

      OrderDetail detail =
          OrderDetail.builder()
              .product(product)
              .unitPrice(unitPrice)
              .instructions(detailCommand.getInstructions())
              .selectedOptions(selectedOptions)
              .selectedProductIds(detailCommand.getSelectedProductIds())
              .additionIds(detailCommand.getAdditionIds())
              .clarifications(detailCommand.getClarifications())
              .build();

      newDetails.add(detail);
    }

    for (OrderDetail detail : newDetails) {
      List<Long> selectedOptionIds =
          detail.getSelectedOptions() != null
              ? detail.getSelectedOptions().stream().map(ProductOption::getId).toList()
              : List.of();

      if (!inventoryStockUseCase.isAvailable(detail.getProduct().getId(), selectedOptionIds)) {
        metricsPort.recordInsufficientStock();
        throw new InsufficientStockException(
            detail.getProduct().getId(),
            aros.services.rms.core.inventory.domain.MovementType.DEDUCTION,
            "Insufficient stock for product: " + detail.getProduct().getName());
      }
    }

    if (order.getDetails() != null && !order.getDetails().isEmpty()) {
      try {
        inventoryMovementUseCase.revertDeductionsForOrder(order.getId(), order.getDetails());
      } catch (Exception e) {
        metricsPort.recordInventoryReversionError();
        throw e;
      }
    }

    order.setDetails(newDetails);
    Order savedOrder = orderRepositoryPort.save(order);

    try {
      inventoryMovementUseCase.deductForOrder(savedOrder.getId(), savedOrder.getDetails());
      metricsPort.recordInventoryDeduction(true);
    } catch (InsufficientStockException e) {
      metricsPort.recordInventoryDeduction(false);
      throw e;
    }

    return savedOrder;
  }

  /**
   * Recovery handler for the cancel operation when the database is unavailable.
   *
   * @param e the data access exception
   * @param id the order identifier
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public Order recoverCancel(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para cancel(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Recovery handler for the update operation when the database is unavailable.
   *
   * @param e the data access exception
   * @param id the order identifier
   * @param command the take order command
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public Order recoverUpdate(DataAccessException e, Long id, TakeOrderCommand command) {
    log.warn("BD no disponible - fallback para update(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
