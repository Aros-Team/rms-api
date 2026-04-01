package aros.services.rms.infraestructure.websocket.adapter;

import aros.services.rms.core.common.notification.port.output.NotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Adapter de infraestructura que implementa {@link NotificationPort} usando STOMP.
 *
 * <p>Publica mensajes a través de {@link SimpMessagingTemplate} hacia el broker en memoria
 * de Spring WebSocket. Opera en modo <em>fire-and-forget</em>: cualquier excepción durante
 * el envío es capturada, registrada con nivel ERROR y no propagada al llamador.
 *
 * <p>El payload es serializado automáticamente a JSON por el conversor de mensajes
 * configurado en la configuración WebSocket.
 */
@Slf4j
@Service
public class WebSocketNotificationAdapter implements NotificationPort {

  private final SimpMessagingTemplate messagingTemplate;

  public WebSocketNotificationAdapter(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @Override
  public void notify(String destination, Object payload) {
    try {
      messagingTemplate.convertAndSend(destination, payload);
      log.info("Notification sent: destination={}", destination);
    } catch (Exception e) {
      log.error("Notification failed: destination={}, error={}", destination, e.getMessage(), e);
    }
  }
}
