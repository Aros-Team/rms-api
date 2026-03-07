/* (C) 2026 */
package aros.services.rms.core.order.application.usecases;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del caso de uso para actualizar órdenes. Permite cancelar o modificar órdenes en
 * estado QUEUE.
 */
public class UpdateOrderUseCaseImpl implements UpdateOrderUseCase {

  private final OrderRepositoryPort orderRepositoryPort;
  private final TableRepositoryPort tableRepositoryPort;
  private final ProductRepositoryPort productRepositoryPort;
  private final ProductOptionRepositoryPort productOptionRepositoryPort;

  public UpdateOrderUseCaseImpl(
      OrderRepositoryPort orderRepositoryPort,
      TableRepositoryPort tableRepositoryPort,
      ProductRepositoryPort productRepositoryPort,
      ProductOptionRepositoryPort productOptionRepositoryPort) {
    this.orderRepositoryPort = orderRepositoryPort;
    this.tableRepositoryPort = tableRepositoryPort;
    this.productRepositoryPort = productRepositoryPort;
    this.productOptionRepositoryPort = productOptionRepositoryPort;
  }

  /** {@inheritDoc} Cancela orden en QUEUE y libera la mesa. */
  @Override
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

    order.setDetails(newDetails);
    return orderRepositoryPort.save(order);
  }
}
