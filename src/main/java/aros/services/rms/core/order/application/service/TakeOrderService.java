/* (C) 2026 */
package aros.services.rms.core.order.application.service;

import aros.services.rms.core.common.notification.port.output.NotificationPort;
import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.port.input.InventoryMovementUseCase;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.order.application.dto.TakeOrderCommand;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.order.port.input.TakeOrderUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.core.product.application.exception.InvalidProductOptionException;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementación del caso de uso para crear órdenes. Valida mesa disponible y opciones de
 * productos.
 */
public class TakeOrderService implements TakeOrderUseCase {

  private static final String QUEUE_DESTINATION = "/topic/orders/created";
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(TakeOrderService.class);

  private final OrderRepositoryPort orderRepositoryPort;
  private final TableRepositoryPort tableRepositoryPort;
  private final ProductRepositoryPort productRepositoryPort;
  private final ProductOptionRepositoryPort productOptionRepositoryPort;
  private final InventoryStockUseCase inventoryStockUseCase;
  private final InventoryMovementUseCase inventoryMovementUseCase;
  private final NotificationPort notificationPort;

  public TakeOrderService(
      OrderRepositoryPort orderRepositoryPort,
      TableRepositoryPort tableRepositoryPort,
      ProductRepositoryPort productRepositoryPort,
      ProductOptionRepositoryPort productOptionRepositoryPort,
      InventoryStockUseCase inventoryStockUseCase,
      InventoryMovementUseCase inventoryMovementUseCase,
      NotificationPort notificationPort) {
    this.orderRepositoryPort = orderRepositoryPort;
    this.tableRepositoryPort = tableRepositoryPort;
    this.productRepositoryPort = productRepositoryPort;
    this.productOptionRepositoryPort = productOptionRepositoryPort;
    this.inventoryStockUseCase = inventoryStockUseCase;
    this.inventoryMovementUseCase = inventoryMovementUseCase;
    this.notificationPort = notificationPort;
  }

  /**
   * {@inheritDoc} Valida mesa disponible, productos y sus opciones. En caso de error libera la
   * mesa.
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Order execute(TakeOrderCommand command) {
    Table table =
        tableRepositoryPort
            .findById(command.getTableId())
            .orElseThrow(() -> new IllegalArgumentException("Table not found"));

    if (table.getStatus() != TableStatus.AVAILABLE) {
      throw new IllegalStateException("Table is not available");
    }

    table.setStatus(TableStatus.OCCUPIED);
    tableRepositoryPort.save(table);

    try {
      Order order =
          Order.builder().table(table).date(LocalDateTime.now()).details(new ArrayList<>()).build();

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

          // Validar que todas las opciones estén asociadas al producto específico
          for (ProductOption option : selectedOptions) {
            if (!productOptionRepositoryPort.isOptionAssociatedWithProduct(
                product.getId(), option.getId())) {
              throw new InvalidProductOptionException(product.getId(), option.getId());
            }
          }
        }

        OrderDetail detail =
            OrderDetail.builder()
                .product(product)
                .unitPrice(product.getBasePrice())
                .instructions(detailCommand.getInstructions())
                .selectedOptions(selectedOptions)
                .build();

        order.getDetails().add(detail);
      }

      // Check inventory availability before saving
      for (OrderDetail detail : order.getDetails()) {
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

      Order savedOrder = orderRepositoryPort.save(order);

      // Deduct inventory after order is saved
      inventoryMovementUseCase.deductForOrder(savedOrder.getId(), savedOrder.getDetails());

      // Publish order creation notification
      notificationPort.notify(QUEUE_DESTINATION, savedOrder);
      log.info("Order created: id={}, destination={}", savedOrder.getId(), QUEUE_DESTINATION);

      return savedOrder;
    } catch (Exception e) {
      table.setStatus(TableStatus.AVAILABLE);
      tableRepositoryPort.save(table);
      throw e;
    }
  }

  @Recover
  public Order recoverExecute(DataAccessException e, TakeOrderCommand command) {
    log.warn(
        "BD no disponible - fallback para execute(tableId={}): {}",
        command.getTableId(),
        e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
