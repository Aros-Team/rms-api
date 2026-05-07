/* (C) 2026 */

package aros.services.rms.infraestructure.websocket.config;

import aros.services.rms.infraestructure.websocket.security.WebSocketChannelInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración del broker de mensajes WebSocket con STOMP y SockJS.
 *
 * <p>Registra el endpoint {@code /ws} con fallback SockJS, habilita el broker en memoria para
 * destinos {@code /topic}, establece el prefijo de aplicación {@code /app} y registra el {@link
 * WebSocketChannelInterceptor} para validar JWT en los frames CONNECT, SEND y SUBSCRIBE.
 *
 * <p>El {@link SimpMessagingTemplate} se inyecta con {@code @Lazy} para evitar la dependencia
 * circular entre el broker de mensajes y el interceptor de canal.
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
  private final SimpMessagingTemplate messagingTemplate;

  @Value("${app.env:development}")
  private String appEnv;

  /**
   * Construye la configuración con el interceptor de canal y la plantilla de mensajería.
   *
   * <p>{@code messagingTemplate} se marca {@code @Lazy} para que Spring lo resuelva después de
   * inicializar el broker, evitando la dependencia circular.
   *
   * @param channelInterceptor interceptor que valida el JWT en los frames STOMP
   * @param messagingTemplate plantilla STOMP inyectada en el interceptor para cerrar sesiones
   */
  public WebSocketConfig(
      WebSocketChannelInterceptor channelInterceptor,
      @Lazy SimpMessagingTemplate messagingTemplate) {
    this.channelInterceptor = channelInterceptor;
    this.messagingTemplate = messagingTemplate;
  }

  /**
   * Configura el broker de mensajes en memoria.
   *
   * <p>Habilita el broker simple para destinos con prefijo {@code /topic} y {@code /queue}, y
   * establece {@code /app} como prefijo para mensajes dirigidos a métodos anotados con
   * {@code @MessageMapping}.
   *
   * @param registry registro del broker de mensajes
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic", "/queue");
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
   * Registra el {@link WebSocketChannelInterceptor} en el canal de entrada del cliente e inyecta el
   * {@link SimpMessagingTemplate} para que el interceptor pueda cerrar sesiones expiradas.
   *
   * @param registration registro del canal de entrada
   */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    channelInterceptor.setMessagingTemplate(messagingTemplate);
    registration.interceptors(channelInterceptor);
  }
}
