/* (C) 2026 */
package aros.services.rms.core.order.application.usecases;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.MarkAsReadyUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementación del caso de uso para marcar órdenes como listas. Permite al cocinero notificar que
 * finalizó la preparación.
 */
public class MarkAsReadyUseCaseImpl implements MarkAsReadyUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(MarkAsReadyUseCaseImpl.class);
  private final OrderRepositoryPort orderRepositoryPort;

  public MarkAsReadyUseCaseImpl(OrderRepositoryPort orderRepositoryPort) {
    this.orderRepositoryPort = orderRepositoryPort;
  }

  /** {@inheritDoc} Cambia el estado de PREPARING a READY. */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Order markAsReady(Long id) {
    Order order =
        orderRepositoryPort
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

    if (order.getStatus() != OrderStatus.PREPARING) {
      throw new IllegalStateException("Order can only be marked as ready when in PREPARING status");
    }

    order.setStatus(OrderStatus.READY);
    return orderRepositoryPort.save(order);
  }

  @Recover
  public Order recoverMarkAsReady(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para markAsReady(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
