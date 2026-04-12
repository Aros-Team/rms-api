/* (C) 2026 */
package aros.services.rms.infraestructure.websocket.security;

import lombok.extern.slf4j.Slf4j;
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

/**
 * Interceptor de canal STOMP que valida el JWT en el frame {@code CONNECT}.
 *
 * <p>Extrae el token del header {@code Authorization} del frame STOMP y lo valida usando el {@link
 * JwtDecoder} configurado en {@link aros.services.rms.config.SecurityConfig}. Si el token es
 * inválido o está ausente, lanza {@link MessageDeliveryException} para rechazar la conexión antes
 * de que el broker la acepte.
 *
 * <p>Solo actúa sobre frames de tipo {@link StompCommand#CONNECT}; el resto de frames pasan sin
 * modificación.
 */
@Slf4j
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

  private final JwtDecoder jwtDecoder;

  /**
   * Construye el interceptor con el decoder JWT existente.
   *
   * @param jwtDecoder decoder JWT configurado con la clave RSA pública del proyecto
   */
  public WebSocketChannelInterceptor(JwtDecoder jwtDecoder) {
    this.jwtDecoder = jwtDecoder;
  }

  /**
   * Intercepta mensajes entrantes del cliente.
   *
   * <p>Para frames {@code CONNECT}, extrae y valida el JWT del header {@code Authorization}. Frames
   * de otros tipos son retornados sin modificación.
   *
   * @param message el mensaje STOMP entrante
   * @param channel el canal por el que llega el mensaje
   * @return el mensaje original si la validación es exitosa
   * @throws MessageDeliveryException si el JWT es inválido, expirado o está ausente
   */
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
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
