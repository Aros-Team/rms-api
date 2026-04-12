/* (C) 2026 */
package aros.services.rms.infraestructure.websocket.config;

import aros.services.rms.infraestructure.websocket.handler.NativeWebSocketHandler;
import aros.services.rms.infraestructure.websocket.security.WebSocketChannelInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración del broker de mensajes WebSocket con STOMP y SockJS.
 *
 * <p>Registra el endpoint {@code /ws} con fallback SockJS, habilita el broker en memoria para
 * destinos {@code /topic}, establece el prefijo de aplicación {@code /app} y registra el {@link
 * WebSocketChannelInterceptor} para validar JWT en el frame CONNECT.
 *
 * <p>Los orígenes permitidos se configuran según el entorno:
 *
 * <ul>
 *   <li>Desarrollo: {@code http://localhost:*}
 *   <li>Producción: {@code https://rms.aros.services}
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private static final String PRODUCTION = "production";

  private final WebSocketChannelInterceptor channelInterceptor;

  @Value("${app.env:development}")
  private String appEnv;

  /**
   * Construye la configuración con el interceptor de canal inyectado.
   *
   * @param channelInterceptor interceptor que valida el JWT en el frame STOMP CONNECT
   */
  public WebSocketConfig(WebSocketChannelInterceptor channelInterceptor) {
    this.channelInterceptor = channelInterceptor;
  }

  /**
   * Configura el broker de mensajes en memoria.
   *
   * <p>Habilita el broker simple para destinos con prefijo {@code /topic} y establece {@code /app}
   * como prefijo para mensajes dirigidos a métodos anotados con {@code @MessageMapping}.
   *
   * @param registry registro del broker de mensajes
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }

  /**
   * Registra el endpoint STOMP con SockJS y configura los orígenes permitidos.
   *
   * @param registry registro de endpoints STOMP
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    boolean isProduction = PRODUCTION.equalsIgnoreCase(appEnv);
    String[] allowedOrigins =
        isProduction
            ? new String[] {"https://rms.aros.services"}
            : new String[] {"http://localhost:*", "http://127.0.0.1:*"};

    registry.addEndpoint("/ws").setAllowedOriginPatterns(allowedOrigins).withSockJS();
  }

  /**
   * Registra el {@link WebSocketChannelInterceptor} en el canal de entrada del cliente.
   *
   * <p>El interceptor valida el JWT en cada frame STOMP de tipo {@code CONNECT} antes de que la
   * conexión sea aceptada por el broker.
   *
   * @param registration registro del canal de entrada
   */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(channelInterceptor);
  }
}
