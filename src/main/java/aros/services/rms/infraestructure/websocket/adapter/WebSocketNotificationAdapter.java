/* (C) 2026 */
package aros.services.rms.infraestructure.websocket.adapter;

import aros.services.rms.core.common.notification.port.output.NotificationPort;
import aros.services.rms.infraestructure.websocket.handler.NativeWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Adapter de infraestructura que implementa {@link NotificationPort} usando STOMP.
 *
 * <p>Publica mensajes a través de {@link SimpMessagingTemplate} hacia el broker en memoria de
 * Spring WebSocket. Opera en modo <em>fire-and-forget</em>: cualquier excepción durante el envío es
 * capturada, registrada con nivel ERROR y no propagada al llamador.
 *
 * <p>El payload es serializado automáticamente a JSON por el conversor de mensajes configurado en
 * {@link aros.services.rms.infraestructure.websocket.config.WebSocketConfig}.
 */
@Slf4j
@Service
public class WebSocketNotificationAdapter implements NotificationPort {

  private final SimpMessagingTemplate messagingTemplate;

  @Autowired(required = false)
  private NativeWebSocketHandler nativeWebSocketHandler;

  /**
   * Construye el adapter con la plantilla de mensajería STOMP.
   *
   * @param messagingTemplate plantilla Spring para enviar mensajes al broker STOMP
   */
  public WebSocketNotificationAdapter(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Envía el payload serializado a JSON al destino STOMP indicado. Si el envío falla, registra
   * el error y retorna sin lanzar excepción.
   *
   * @param destination ruta STOMP destino, p.ej. {@code /topic/orders/ready}
   * @param payload objeto a serializar; será convertido a JSON por el message converter
   */
  @Override
  public void notify(String destination, Object payload) {
    try {
      messagingTemplate.convertAndSend(destination, payload);
      log.info("Notification sent: destination={}", destination);
    } catch (Exception e) {
      log.error("Notification failed: destination={}, error={}", destination, e.getMessage(), e);
    }

    // También enviar a clientes WebSocket nativos (si está disponible)
    if (nativeWebSocketHandler != null) {
      try {
        String eventType = extractEventType(destination);
        nativeWebSocketHandler.broadcastOrderUpdate(eventType, payload);
      } catch (Exception e) {
        log.error("Failed to broadcast to native WebSocket clients: destination={}, error={}",
                  destination, e.getMessage(), e);
      }
    }
  }

  /**
   * Extrae el tipo de evento del destination STOMP.
   *
   * @param destination ruta STOMP, ej. "/topic/orders/ready"
   * @return tipo de evento, ej. "order-ready"
   */
  private String extractEventType(String destination) {
    if (destination.contains("/topic/orders/created")) {
      return "order-created";
    } else if (destination.contains("/topic/orders/preparing")) {
      return "order-preparing";
    } else if (destination.contains("/topic/orders/ready")) {
      return "order-ready";
    } else {
      return "order-update";
    }
  }
}
