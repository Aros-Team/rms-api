/* (C) 2026 */

package aros.services.rms.infraestructure.order.api;

import aros.services.rms.core.common.notification.port.output.NotificationPort;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio de notificaciones en tiempo real para órdenes. Publica eventos de ciclo de vida de
 * órdenes a los clientes WebSocket suscritos a través del {@link NotificationPort}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationService {

  private static final String TOPIC_ORDERS_CREATED = "/topic/orders/created";
  private static final String TOPIC_ORDERS_PREPARING = "/topic/orders/preparing";
  private static final String TOPIC_ORDERS_READY = "/topic/orders/ready";
  private static final String TOPIC_ORDERS_DELIVERED = "/topic/orders/delivered";
  private static final String TOPIC_ORDERS_CANCELLED = "/topic/orders/cancelled";

  private final NotificationPort notificationPort;

  /** Notifica a los clientes que una nueva orden fue creada (estado QUEUE). */
  public void notifyOrderCreated(OrderResponse order) {
    log.info("WebSocket: notificando orden creada id={}", order.id());
    notificationPort.notify(TOPIC_ORDERS_CREATED, order);
  }

  /** Notifica a los clientes que una orden pasó a estado PREPARING. */
  public void notifyOrderPreparing(OrderResponse order) {
    log.info("WebSocket: notificando orden en preparación id={}", order.id());
    notificationPort.notify(TOPIC_ORDERS_PREPARING, order);
  }

  /** Notifica a los clientes que una orden pasó a estado READY. */
  public void notifyOrderReady(OrderResponse order) {
    log.info("WebSocket: notificando orden lista id={}", order.id());
    notificationPort.notify(TOPIC_ORDERS_READY, order);
  }

  /** Notifica a los clientes que una orden pasó a estado DELIVERED. */
  public void notifyOrderDelivered(OrderResponse order) {
    log.info("WebSocket: notificando orden entregada id={}", order.id());
    notificationPort.notify(TOPIC_ORDERS_DELIVERED, order);
  }

  /** Notifica a los clientes que una orden fue cancelada (estado CANCELLED). */
  public void notifyOrderCancelled(OrderResponse order) {
    log.info("WebSocket: notificando orden cancelada id={}", order.id());
    notificationPort.notify(TOPIC_ORDERS_CANCELLED, order);
  }
}
