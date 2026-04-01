/* (C) 2026 */
package aros.services.rms.core.order.application.usecases;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.notification.port.output.NotificationPort;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.MarkAsReadyUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementación del caso de uso para marcar órdenes como listas. Permite al cocinero notificar que
 * finalizó la preparación.
 */
public class MarkAsReadyUseCaseImpl implements MarkAsReadyUseCase {

  private static final String READY_DESTINATION = "/topic/orders/ready";

  private final OrderRepositoryPort orderRepositoryPort;
  private final NotificationPort notificationPort;
  private final Logger logger;

  public MarkAsReadyUseCaseImpl(
      OrderRepositoryPort orderRepositoryPort, NotificationPort notificationPort, Logger logger) {
    this.orderRepositoryPort = orderRepositoryPort;
    this.notificationPort = notificationPort;
    this.logger = logger;
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
    Order saved = orderRepositoryPort.save(order);

    // Notificación fire-and-forget (el adapter captura excepciones internamente)
    notificationPort.notify(READY_DESTINATION, saved);
    logger.info("Order marked as ready: id={}, destination={}", id, READY_DESTINATION);

    return saved;
  }

  @Recover
  public Order recoverMarkAsReady(DataAccessException e, Long id) {
    logger.info("BD no disponible - fallback para markAsReady(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
