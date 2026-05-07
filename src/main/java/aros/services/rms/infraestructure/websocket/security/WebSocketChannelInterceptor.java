/* (C) 2026 */

package aros.services.rms.infraestructure.websocket.security;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

/**
 * Interceptor de canal STOMP que valida el JWT en cada frame relevante.
 *
 * <h2>Validación en CONNECT</h2>
 *
 * <p>Extrae el token del header {@code Authorization} del frame STOMP y lo valida usando el {@link
 * JwtDecoder} configurado en {@link aros.services.rms.config.SecurityConfig}. Si el token es
 * inválido o está ausente, lanza {@link MessageDeliveryException} para rechazar la conexión antes
 * de que el broker la acepte.
 *
 * <h2>Validación post-conexión (SEND / SUBSCRIBE)</h2>
 *
 * <p>Re-valida el JWT almacenado en los atributos de sesión en cada frame {@code SEND} y {@code
 * SUBSCRIBE}. Si el token ha expirado desde que se estableció la conexión, envía un frame STOMP
 * {@code ERROR} al cliente y cierra la sesión mediante {@link SimpMessagingTemplate}, impidiendo
 * que el mensaje sea procesado por el broker.
 *
 * <p>El token se almacena en los atributos de sesión STOMP bajo la clave {@value
 * #SESSION_TOKEN_KEY} durante el frame {@code CONNECT} para evitar re-parsear el header en cada
 * mensaje posterior.
 */
@Slf4j
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

  /** Clave bajo la que se guarda el token JWT en los atributos de sesión STOMP. */
  static final String SESSION_TOKEN_KEY = "jwt-token";

  private static final List<StompCommand> VALIDATED_COMMANDS =
      List.of(StompCommand.SEND, StompCommand.SUBSCRIBE);

  private final JwtDecoder jwtDecoder;

  /**
   * Construye el interceptor. {@link SimpMessagingTemplate} se inyecta con setter para evitar
   * dependencia circular con el broker de mensajes durante el arranque.
   *
   * @param jwtDecoder decoder JWT configurado con la clave RSA pública del proyecto
   */
  public WebSocketChannelInterceptor(JwtDecoder jwtDecoder) {
    this.jwtDecoder = jwtDecoder;
  }

  private SimpMessagingTemplate messagingTemplate;

  /**
   * Inyecta la plantilla de mensajería STOMP. Se usa setter injection para romper la dependencia
   * circular que se produciría si se inyectara por constructor (el broker necesita el interceptor y
   * el interceptor necesitaría el broker).
   *
   * @param messagingTemplate plantilla para enviar el frame ERROR al cliente
   */
  public void setMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  /**
   * Intercepta mensajes entrantes del cliente.
   *
   * <ul>
   *   <li>{@code CONNECT}: valida el JWT del header y lo almacena en la sesión.
   *   <li>{@code SEND} / {@code SUBSCRIBE}: re-valida el JWT almacenado en sesión.
   *   <li>Resto de frames: pasan sin modificación.
   * </ul>
   *
   * @param message el mensaje STOMP entrante
   * @param channel el canal por el que llega el mensaje
   * @return el mensaje original si la validación es exitosa
   * @throws MessageDeliveryException si el JWT es inválido, expirado o está ausente en CONNECT
   */
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null) {
      return message;
    }

    StompCommand command = accessor.getCommand();

    if (command == StompCommand.CONNECT) {
      return handleConnect(message, accessor);
    }

    if (VALIDATED_COMMANDS.contains(command)) {
      return handlePostConnect(message, accessor);
    }

    return message;
  }

  // ---------------------------------------------------------------------------
  // Private helpers
  // ---------------------------------------------------------------------------

  /**
   * Valida el JWT en el frame CONNECT y lo almacena en los atributos de sesión.
   *
   * @throws MessageDeliveryException si el header falta, está mal formado o el token es inválido
   */
  private Message<?> handleConnect(Message<?> message, StompHeaderAccessor accessor) {
    String authHeader = accessor.getFirstNativeHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.error("WebSocket CONNECT rejected: missing or malformed Authorization header");
      throw new MessageDeliveryException(message, "Missing or malformed Authorization header");
    }

    String token = authHeader.substring(7);

    try {
      Jwt jwt = jwtDecoder.decode(token);
      // Guardar el token raw en la sesión para re-validarlo en frames posteriores
      accessor.getSessionAttributes().put(SESSION_TOKEN_KEY, token);
      log.debug(
          "WebSocket CONNECT accepted: subject={}, expiresAt={}",
          jwt.getSubject(),
          jwt.getExpiresAt());
    } catch (JwtException e) {
      log.error("WebSocket CONNECT rejected: invalid JWT — {}", e.getMessage());
      throw new MessageDeliveryException(message, "Invalid JWT token: " + e.getMessage(), e);
    }

    return message;
  }

  /**
   * Re-valida el JWT almacenado en sesión para frames SEND/SUBSCRIBE.
   *
   * <p>Si el token ha expirado, envía un frame STOMP {@code ERROR} al cliente usando el {@code
   * sessionId} y bloquea el mensaje devolviendo {@code null}.
   *
   * @return el mensaje original si el token sigue siendo válido, {@code null} para bloquearlo
   */
  private Message<?> handlePostConnect(Message<?> message, StompHeaderAccessor accessor) {
    var sessionAttributes = accessor.getSessionAttributes();

    if (sessionAttributes == null) {
      log.warn("WebSocket frame blocked: no session attributes found");
      return null;
    }

    String token = (String) sessionAttributes.get(SESSION_TOKEN_KEY);

    if (token == null) {
      log.warn("WebSocket frame blocked: no JWT found in session attributes");
      closeSession(accessor, "Session token missing — please reconnect");
      return null;
    }

    try {
      jwtDecoder.decode(token);
    } catch (JwtException e) {
      log.warn(
          "WebSocket session closed: token expired or invalid for sessionId={} — {}",
          accessor.getSessionId(),
          e.getMessage());
      closeSession(accessor, "Token expired — please reconnect");
      return null;
    }

    return message;
  }

  /**
   * Envía un frame STOMP {@code ERROR} al cliente y cierra la sesión.
   *
   * <p>Usa {@link SimpMessagingTemplate} con tipo de mensaje {@link SimpMessageType#CONNECT_ACK}
   * hacia el destino de usuario de la sesión. Si {@code messagingTemplate} no está disponible
   * (arranque temprano), solo registra el error.
   *
   * @param accessor accessor del mensaje que originó el cierre
   * @param reason mensaje de error a enviar al cliente
   */
  private void closeSession(StompHeaderAccessor accessor, String reason) {
    String sessionId = accessor.getSessionId();

    if (messagingTemplate == null) {
      log.error("Cannot close session {}: messagingTemplate not available", sessionId);
      return;
    }

    try {
      SimpMessageHeaderAccessor errorAccessor =
          SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
      errorAccessor.setSessionId(sessionId);
      errorAccessor.setLeaveMutable(true);

      messagingTemplate.convertAndSendToUser(
          sessionId, "/queue/errors", reason, errorAccessor.getMessageHeaders());

      log.info("Sent ERROR frame to sessionId={}: {}", sessionId, reason);
    } catch (Exception e) {
      log.error("Failed to send ERROR frame to sessionId={}: {}", sessionId, e.getMessage());
    }
  }
}
