/* (C) 2026 */

package aros.services.rms.core.order.application.service;

import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.MarkAsReadyUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.time.LocalDateTime;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementación del caso de uso para marcar órdenes como listas. Permite al cocinero notificar que
 * finalizar la preparación.
 */
public class MarkAsReadyService implements MarkAsReadyUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(MarkAsReadyService.class);
  private final OrderRepositoryPort orderRepositoryPort;
  private final BusinessMetricsPort metricsPort;

  /**
   * Creates a new mark as ready service instance.
   *
   * @param orderRepositoryPort the order repository port
   * @param metricsPort the business metrics port
   */
  public MarkAsReadyService(
      OrderRepositoryPort orderRepositoryPort, BusinessMetricsPort metricsPort) {
    this.orderRepositoryPort = orderRepositoryPort;
    this.metricsPort = metricsPort;
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

    LocalDateTime readyAt = LocalDateTime.now();
    order.setStatus(OrderStatus.READY);
    Order savedOrder = orderRepositoryPort.save(order);

    metricsPort.recordOrderStatusTransition("PREPARING", "READY");
    metricsPort.recordNotificationLatency(order.getId(), order.getDate(), readyAt);

    return savedOrder;
  }

  /**
   * Recovery handler for markAsReady operation when database is unavailable.
   *
   * @param e the data access exception
   * @param id the order identifier
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public Order recoverMarkAsReady(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para markAsReady(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
