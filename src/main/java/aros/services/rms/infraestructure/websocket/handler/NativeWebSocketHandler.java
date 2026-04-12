/* (C) 2026 */
package aros.services.rms.infraestructure.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Handler para conexiones WebSocket nativas sin STOMP.
 *
 * <p>Permite que clientes que no usan STOMP puedan conectarse directamente vía WebSocket
 * y suscribirse a notificaciones de órdenes. Los mensajes se envían en formato JSON simple.
 */
@Slf4j
@Component
public class NativeWebSocketHandler extends TextWebSocketHandler {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    log.info("WebSocket nativo conectado: sessionId={}", session.getId());
    sessions.add(session);
    // Aquí podríamos implementar lógica de suscripción si es necesario
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload();
    log.debug("Mensaje recibido en WebSocket nativo: sessionId={}, payload={}", session.getId(), payload);

    // Por ahora, solo logueamos. En el futuro podríamos implementar comandos de suscripción
    session.sendMessage(new TextMessage("{\"type\":\"ack\",\"message\":\"Connected to native WebSocket\"}"));
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    log.info("WebSocket nativo desconectado: sessionId={}, status={}", session.getId(), status);
    sessions.remove(session);
  }

  /**
   * Envía una notificación a todos los clientes WebSocket nativos conectados.
   * Este método sería llamado desde los use cases.
   */
  public void broadcastOrderUpdate(String eventType, Object orderData) {
    // TODO: Implementar broadcasting a todas las sesiones conectadas
    log.info("Broadcasting {} to native WebSocket clients: {}", eventType, orderData);
    String message;
    try {
      message = objectMapper.writeValueAsString(orderData);
    } catch (IOException e) {
      log.error("Error serializando el objeto orderData a JSON", e);
      return;
    }

    for (WebSocketSession session : sessions) {
      if (session.isOpen()) {
        try {
          session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
          log.error("Error enviando mensaje a la sesión WebSocket: sessionId={}", session.getId(), e);
        }
      }
    }
  }
}
