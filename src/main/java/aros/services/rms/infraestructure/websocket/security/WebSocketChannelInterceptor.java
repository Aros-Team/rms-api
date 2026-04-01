package aros.services.rms.infraestructure.websocket.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

  private final @Nullable JwtDecoder jwtDecoder;

  public WebSocketChannelInterceptor(@Nullable JwtDecoder jwtDecoder) {
    this.jwtDecoder = jwtDecoder;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
      return message;
    }

    if (jwtDecoder == null) {
      // Entorno sin JWT configurado: permitir conexión (e.g., local/dev) y loguear advertencia.
      log.warn("WebSocket CONNECT without JWT validation: JwtDecoder bean is not configured");
      return message;
    }

    String authHeader = accessor.getFirstNativeHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.error("WebSocket connection rejected: missing or malformed Authorization header");
      throw new MessageDeliveryException(message, "Missing or malformed Authorization header");
    }

    String token = authHeader.substring(7);

    try {
      var jwt = jwtDecoder.decode(token);
      log.debug("WebSocket connection accepted: subject={}", jwt.getSubject());
    } catch (JwtException e) {
      log.error("WebSocket connection rejected: invalid JWT, error={}", e.getMessage());
      throw new MessageDeliveryException(message, "Invalid JWT token: " + e.getMessage(), e);
    }

    return message;
  }
}
