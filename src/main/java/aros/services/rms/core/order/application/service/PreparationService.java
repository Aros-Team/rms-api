/* (C) 2026 */
package aros.services.rms.core.order.application.service;

import aros.services.rms.core.common.notification.port.output.NotificationPort;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.PreparationUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
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

  private static final String PREPARING_DESTINATION = "/topic/orders/preparing";
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(PreparationService.class);

  private final OrderRepositoryPort orderRepositoryPort;
  private final NotificationPort notificationPort;

  public PreparationService(
      OrderRepositoryPort orderRepositoryPort, NotificationPort notificationPort) {
    this.orderRepositoryPort = orderRepositoryPort;
    this.notificationPort = notificationPort;
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
            .orElseThrow(() -> new IllegalStateException("No orders in queue"));

    order.setStatus(OrderStatus.PREPARING);
    Order savedOrder = orderRepositoryPort.save(order);

    // Publish order preparation notification
    notificationPort.notify(PREPARING_DESTINATION, savedOrder);
    log.info(
        "Order moved to preparation: id={}, destination={}",
        savedOrder.getId(),
        PREPARING_DESTINATION);

    return savedOrder;
  }

  @Recover
  public Order recoverProcessNextOrder(DataAccessException e) {
    log.warn("BD no disponible - fallback para processNextOrder: {}", e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
