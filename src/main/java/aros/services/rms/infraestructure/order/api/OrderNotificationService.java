/* (C) 2026 */
package aros.services.rms.infraestructure.order.api;

import aros.services.rms.infraestructure.order.api.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio de notificaciones en tiempo real para órdenes. Publica eventos de ciclo de vida de
 * órdenes a los clientes WebSocket suscritos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationService {

  private static final String TOPIC_ORDERS_CREATED = "/topic/orders/created";
  private static final String TOPIC_ORDERS_PREPARING = "/topic/orders/preparing";
  private static final String TOPIC_ORDERS_READY = "/topic/orders/ready";

  private final SimpMessagingTemplate messagingTemplate;

  /** Notifica a los clientes que una nueva orden fue creada (estado QUEUE). */
  public void notifyOrderCreated(OrderResponse order) {
    log.info("WebSocket: notificando orden creada id={}", order.id());
    messagingTemplate.convertAndSend(TOPIC_ORDERS_CREATED, order);
  }

  /** Notifica a los clientes que una orden pasó a estado PREPARING. */
  public void notifyOrderPreparing(OrderResponse order) {
    log.info("WebSocket: notificando orden en preparación id={}", order.id());
    messagingTemplate.convertAndSend(TOPIC_ORDERS_PREPARING, order);
  }

  /** Notifica a los clientes que una orden pasó a estado READY. */
  public void notifyOrderReady(OrderResponse order) {
    log.info("WebSocket: notificando orden lista id={}", order.id());
    messagingTemplate.convertAndSend(TOPIC_ORDERS_READY, order);
  }
}
