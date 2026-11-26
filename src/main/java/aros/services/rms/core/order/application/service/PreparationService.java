/* (C) 2026 */

package aros.services.rms.core.order.application.service;

import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.order.application.exception.OrderDetailNotFoundException;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.PreparationUseCase;
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
 * Implementación del caso de uso para pasar órdenes a preparación. Toma la orden más antigua de la
 * cola.
 */
public class PreparationService implements PreparationUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(PreparationService.class);
  private final OrderRepositoryPort orderRepositoryPort;
  private final BusinessMetricsPort metricsPort;

  /**
   * Creates a new preparation service instance.
   *
   * @param orderRepositoryPort the order repository port
   * @param metricsPort the business metrics port
   */
  public PreparationService(
      OrderRepositoryPort orderRepositoryPort, BusinessMetricsPort metricsPort) {
    this.orderRepositoryPort = orderRepositoryPort;
    this.metricsPort = metricsPort;
  }

  /** {@inheritDoc} Busca la orden más antigua en QUEUE y cambia a PREPARING. */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Order processNextOrder() {
    Order order =
        orderRepositoryPort
            .findFirstByStatusOrderByDateAsc(OrderStatus.QUEUE)
            .orElseThrow(() -> new IllegalStateException("No hay pedidos en cola"));

    return startPreparation(order);
  }

  /** {@inheritDoc} */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Order startDetail(Long orderId, Long detailId) {
    Order order =
        orderRepositoryPort
            .findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
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
    LocalDateTime now = LocalDateTime.now();
    if (detail.getPreparationStartedAt() == null) {
      detail.setPreparationStartedAt(now);
    }
    if (order.getStatus() == OrderStatus.QUEUE) {
      order.setStatus(OrderStatus.PREPARING);
    }
    return orderRepositoryPort.save(order);
  }

  /** {@inheritDoc} */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Order startArea(Long orderId, Long areaId) {
    Order order =
        orderRepositoryPort
            .findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    LocalDateTime now = LocalDateTime.now();
    boolean startedAny = false;
    if (order.getDetails() != null) {
      for (OrderDetail detail : order.getDetails()) {
        if (detail == null
            || detail.getProduct() == null
            || !Objects.equals(detail.getProduct().getPreparationAreaId(), areaId)) {
          continue;
        }
        if (detail.getPreparationStartedAt() == null) {
          detail.setPreparationStartedAt(now);
          startedAny = true;
        }
      }
    }
    if (startedAny && order.getStatus() == OrderStatus.QUEUE) {
      order.setStatus(OrderStatus.PREPARING);
    }
    return orderRepositoryPort.save(order);
  }

  /** {@inheritDoc} Cambia a PREPARING la orden indicada, validando que siga en QUEUE. */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Order prepareOrder(Long id) {
    Order order =
        orderRepositoryPort.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

    if (order.getStatus() != OrderStatus.QUEUE) {
      throw new IllegalStateException(
          "El pedido solo se puede preparar cuando está en estado QUEUE");
    }

    return startPreparation(order);
  }

  private Order startPreparation(Order order) {
    LocalDateTime preparationStartedAt = LocalDateTime.now();
    order.setStatus(OrderStatus.PREPARING);
    Order savedOrder = orderRepositoryPort.save(order);

    metricsPort.recordOrderStatusTransition("QUEUE", "PREPARING");
    metricsPort.recordKitchenLatency(order.getId(), order.getDate(), preparationStartedAt);

    return savedOrder;
  }

  /**
   * Recovery handler for processNextOrder operation when database is unavailable.
   *
   * @param e the data access exception
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public Order recoverProcessNextOrder(DataAccessException e) {
    log.warn("BD no disponible - fallback para processNextOrder: {}", e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Recovery handler for prepareOrder operation when database is unavailable.
   *
   * @param e the data access exception
   * @param id the order identifier
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public Order recoverPrepareOrder(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para prepareOrder(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
