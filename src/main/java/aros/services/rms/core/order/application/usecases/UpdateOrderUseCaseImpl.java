/* (C) 2026 */
package aros.services.rms.core.order.application.usecases;

import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.port.input.InventoryMovementUseCase;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.UpdateOrderUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementación del caso de uso para actualizar órdenes. Permite cancelar o modificar órdenes en
 * estado QUEUE.
 */
public class UpdateOrderUseCaseImpl implements UpdateOrderUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(UpdateOrderUseCaseImpl.class);
  private final OrderRepositoryPort orderRepositoryPort;
  private final TableRepositoryPort tableRepositoryPort;
  private final ProductRepositoryPort productRepositoryPort;
  private final ProductOptionRepositoryPort productOptionRepositoryPort;
  private final InventoryStockUseCase inventoryStockUseCase;
  private final InventoryMovementUseCase inventoryMovementUseCase;

  public UpdateOrderUseCaseImpl(
      OrderRepositoryPort orderRepositoryPort,
      TableRepositoryPort tableRepositoryPort,
      ProductRepositoryPort productRepositoryPort,
      ProductOptionRepositoryPort productOptionRepositoryPort,
      InventoryStockUseCase inventoryStockUseCase,
      InventoryMovementUseCase inventoryMovementUseCase) {
    this.orderRepositoryPort = orderRepositoryPort;
    this.tableRepositoryPort = tableRepositoryPort;
    this.productRepositoryPort = productRepositoryPort;
    this.productOptionRepositoryPort = productOptionRepositoryPort;
    this.inventoryStockUseCase = inventoryStockUseCase;
    this.inventoryMovementUseCase = inventoryMovementUseCase;
  }

  /** {@inheritDoc} Cancela orden en QUEUE y libera la mesa. */
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

    return savedOrder;
  }

  /** {@inheritDoc} Actualiza detalles de orden en QUEUE. Valida productos y opciones. */
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

      if (product.isHasOptions() && !hasSelectedOptions) {
        throw new IllegalArgumentException(
            "Product '" + product.getName() + "' requires options to be selected");
      }

      if (!product.isHasOptions() && hasSelectedOptions) {
        throw new IllegalArgumentException(
            "Product '" + product.getName() + "' does not support options");
      }

      List<ProductOption> selectedOptions = new ArrayList<>();
      if (hasSelectedOptions) {
        selectedOptions =
            productOptionRepositoryPort.findAllById(detailCommand.getSelectedOptionIds());
      }

      OrderDetail detail =
          OrderDetail.builder()
              .product(product)
              .unitPrice(product.getBasePrice())
              .instructions(detailCommand.getInstructions())
              .selectedOptions(selectedOptions)
              .build();

      newDetails.add(detail);
    }

    // Check inventory availability for new details
    for (OrderDetail detail : newDetails) {
      List<Long> selectedOptionIds =
          detail.getSelectedOptions() != null
              ? detail.getSelectedOptions().stream().map(ProductOption::getId).toList()
              : List.of();

      if (!inventoryStockUseCase.isAvailable(detail.getProduct().getId(), selectedOptionIds)) {
        throw new InsufficientStockException(
            detail.getProduct().getId(),
            aros.services.rms.core.inventory.domain.MovementType.DEDUCTION,
            "Insufficient stock for product: " + detail.getProduct().getName());
      }
    }

    // Revert previous inventory deductions (return stock to locations)
    if (order.getDetails() != null && !order.getDetails().isEmpty()) {
      inventoryMovementUseCase.revertDeductionsForOrder(order.getId(), order.getDetails());
    }

    order.setDetails(newDetails);
    Order savedOrder = orderRepositoryPort.save(order);

    // Deduct inventory for new details
    inventoryMovementUseCase.deductForOrder(savedOrder.getId(), savedOrder.getDetails());

    return savedOrder;
  }

  @Recover
  public Order recoverCancel(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para cancel(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  @Recover
  public Order recoverUpdate(DataAccessException e, Long id, TakeOrderCommand command) {
    log.warn("BD no disponible - fallback para update(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
