/* (C) 2026 */

package aros.services.rms.core.order.application.service;

import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.order.application.exception.OrderDetailNotFoundException;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.MarkAsReadyUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.time.LocalDateTime;
import java.util.Objects;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementación del caso de uso para marcar órdenes como listas. Permite al cocinero notificar que
 * finalizar la preparación, ya sea la orden completa o una línea en su estación.
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

  /**
   * {@inheritDoc} Cambia el estado de PREPARING a READY. Las líneas pendientes quedan también
   * marcadas como listas para que la vista por ítem sea coherente.
   */
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
    markAllDetailsReady(order, readyAt);
    order.setStatus(OrderStatus.READY);
    Order savedOrder = orderRepositoryPort.save(order);

    metricsPort.recordOrderStatusTransition("PREPARING", "READY");
    metricsPort.recordNotificationLatency(order.getId(), order.getDate(), readyAt);

    return savedOrder;
  }

  /**
   * {@inheritDoc} Marca una línea como lista y completa la orden si ya no queda ninguna pendiente.
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Order markDetailAsReady(Long orderId, Long detailId) {
    Order order =
        orderRepositoryPort
            .findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (order.getStatus() != OrderStatus.PREPARING && order.getStatus() != OrderStatus.READY) {
      throw new IllegalStateException(
          "Order details can only be marked as ready when in PREPARING status");
    }

    OrderDetail detail =
        order.getDetails() == null
            ? null
            : order.getDetails().stream()
                .filter(
                    candidate -> candidate != null && Objects.equals(detailId, candidate.getId()))
                .findFirst()
                .orElse(null);

    if (detail == null) {
      throw new OrderDetailNotFoundException(orderId, detailId);
    }

    if (detail.isReady()) {
      return order;
    }

    final boolean wasPreparing = order.getStatus() == OrderStatus.PREPARING;
    LocalDateTime readyAt = LocalDateTime.now();
    detail.setReady(true);
    detail.setReadyAt(readyAt);

    boolean allReady =
        order.getDetails().stream().filter(Objects::nonNull).allMatch(OrderDetail::isReady);
    if (allReady) {
      order.setStatus(OrderStatus.READY);
    }

    Order savedOrder = orderRepositoryPort.save(order);

    if (allReady && wasPreparing) {
      metricsPort.recordOrderStatusTransition("PREPARING", "READY");
      metricsPort.recordNotificationLatency(savedOrder.getId(), savedOrder.getDate(), readyAt);
    }

    return savedOrder;
  }

  private void markAllDetailsReady(Order order, LocalDateTime readyAt) {
    if (order.getDetails() == null) {
      return;
    }
    for (OrderDetail detail : order.getDetails()) {
      if (detail != null && !detail.isReady()) {
        detail.setReady(true);
        detail.setReadyAt(readyAt);
      }
    }
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

  /**
   * Recovery handler for markDetailAsReady operation when database is unavailable.
   *
   * @param e the data access exception
   * @param orderId the order identifier
   * @param detailId the order detail identifier
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public Order recoverMarkDetailAsReady(DataAccessException e, Long orderId, Long detailId) {
    log.warn(
        "BD no disponible - fallback para markDetailAsReady(orderId={}, detailId={}): {}",
        orderId,
        detailId,
        e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
