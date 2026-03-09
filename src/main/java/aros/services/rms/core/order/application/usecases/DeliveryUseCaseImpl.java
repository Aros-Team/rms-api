/* (C) 2026 */
package aros.services.rms.core.order.application.usecases;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.DeliveryUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementación del caso de uso para entregar órdenes al cliente. Marca la orden como entregada y
 * libera la mesa.
 */
public class DeliveryUseCaseImpl implements DeliveryUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(DeliveryUseCaseImpl.class);
  private final OrderRepositoryPort orderRepositoryPort;
  private final TableRepositoryPort tableRepositoryPort;

  public DeliveryUseCaseImpl(
      OrderRepositoryPort orderRepositoryPort, TableRepositoryPort tableRepositoryPort) {
    this.orderRepositoryPort = orderRepositoryPort;
    this.tableRepositoryPort = tableRepositoryPort;
  }

  /** {@inheritDoc} Cambia el estado de READY a DELIVERED y libera la mesa. */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Order markAsDelivered(Long id) {
    Order order =
        orderRepositoryPort
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

    if (order.getStatus() != OrderStatus.READY) {
      throw new IllegalStateException("Order can only be delivered when in READY status");
    }

    order.setStatus(OrderStatus.DELIVERED);
    Order savedOrder = orderRepositoryPort.save(order);

    if (order.getTable() != null) {
      Table table = order.getTable();
      table.setStatus(TableStatus.AVAILABLE);
      tableRepositoryPort.save(table);
    }

    return savedOrder;
  }

  @Recover
  public Order recoverMarkAsDelivered(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para markAsDelivered(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
