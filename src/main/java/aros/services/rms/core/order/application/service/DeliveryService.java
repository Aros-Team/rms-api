/* (C) 2026 */

package aros.services.rms.core.order.application.service;

import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.order.application.exception.OrderDetailNotFoundException;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.DeliveryUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.time.LocalDateTime;
import java.util.Objects;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementación del caso de uso para entregar órdenes al cliente. Marca la orden como entregada y
 * libera la mesa.
 */
public class DeliveryService implements DeliveryUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(DeliveryService.class);
  private final OrderRepositoryPort orderRepositoryPort;
  private final TableRepositoryPort tableRepositoryPort;
  private final BusinessMetricsPort metricsPort;

  /**
   * Creates a new delivery service instance.
   *
   * @param orderRepositoryPort the order repository port
   * @param tableRepositoryPort the table repository port
   * @param metricsPort the business metrics port
   */
  public DeliveryService(
      OrderRepositoryPort orderRepositoryPort,
      TableRepositoryPort tableRepositoryPort,
      BusinessMetricsPort metricsPort) {
    this.orderRepositoryPort = orderRepositoryPort;
    this.tableRepositoryPort = tableRepositoryPort;
    this.metricsPort = metricsPort;
  }

  /** {@inheritDoc} */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Order markDetailAsDelivered(Long orderId, Long detailId) {
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
    if (!detail.isReady()) {
      throw new IllegalStateException("La línea todavía no está lista para entregar");
    }
    if (detail.isDelivered()) {
      return order;
    }
    LocalDateTime deliveredAt = LocalDateTime.now();
    detail.setDelivered(true);
    detail.setDeliveredAt(deliveredAt);
    boolean allDelivered =
        order.getDetails().stream().filter(Objects::nonNull).allMatch(OrderDetail::isDelivered);
    if (allDelivered) {
      order.setStatus(OrderStatus.DELIVERED);
      order.setDeliveredAt(deliveredAt);
      if (order.getCloseTime() == null) {
        order.setCloseTime(deliveredAt);
      }
    }
    Order saved = orderRepositoryPort.save(order);
    if (allDelivered) {
      releaseTableIfNeeded(saved);
      metricsPort.recordOrderStatusTransition("READY", "DELIVERED");
      metricsPort.recordOrderDelivery();
    }
    return saved;
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
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

    if (order.getStatus() != OrderStatus.READY) {
      throw new IllegalStateException(
          "El pedido solo se puede entregar cuando está en estado READY");
    }

    boolean hasPendingLines =
        order.getDetails() != null
            && order.getDetails().stream()
                .filter(Objects::nonNull)
                .anyMatch(detail -> !detail.isReady());
    if (hasPendingLines) {
      throw new IllegalStateException("Todavía hay líneas sin terminar en el pedido");
    }
    LocalDateTime deliveredAt = LocalDateTime.now();
    if (order.getDetails() != null) {
      for (OrderDetail detail : order.getDetails()) {
        if (detail != null && !detail.isDelivered()) {
          detail.setDelivered(true);
          detail.setDeliveredAt(deliveredAt);
        }
      }
    }
    order.setStatus(OrderStatus.DELIVERED);
    order.setDeliveredAt(deliveredAt);
    if (order.getCloseTime() == null) {
      order.setCloseTime(deliveredAt);
    }
    Order savedOrder = orderRepositoryPort.save(order);

    releaseTableIfNeeded(savedOrder);
    metricsPort.recordOrderStatusTransition("READY", "DELIVERED");
    metricsPort.recordOrderDelivery();
    return savedOrder;
  }

  private void releaseTableIfNeeded(Order order) {
    if (order.getTable() != null) {
      Table table = order.getTable();
      table.setStatus(TableStatus.AVAILABLE);
      tableRepositoryPort.save(table);
    }
  }

  /**
   * Recovery handler for markAsDelivered operation when database is unavailable.
   *
   * @param e the data access exception
   * @param id the order identifier
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public Order recoverMarkAsDelivered(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para markAsDelivered(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
