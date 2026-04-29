/* (C) 2026 */
package aros.services.rms.infraestructure.websocket.config;

import aros.services.rms.infraestructure.websocket.handler.NativeWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configuración adicional para WebSocket nativo sin STOMP.
 *
 * <p>Permite conexiones WebSocket nativas directas para clientes que no usan el protocolo STOMP.
 * Complementa la configuración STOMP/SockJS para mayor compatibilidad.
 */
@Configuration
@EnableWebSocket
public class NativeWebSocketConfig implements WebSocketConfigurer {

  private static final String PRODUCTION = "production";

  private final NativeWebSocketHandler nativeWebSocketHandler;

  @Value("${app.env:development}")
  private String appEnv;

  /**
   * Creates a new instance.
   *
   * @param nativeWebSocketHandler the WebSocket handler
   */
  public NativeWebSocketConfig(NativeWebSocketHandler nativeWebSocketHandler) {
    this.nativeWebSocketHandler = nativeWebSocketHandler;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    boolean isProduction = PRODUCTION.equalsIgnoreCase(appEnv);
    String[] allowedOrigins =
        isProduction
            ? new String[] {"https://rms.aros.services"}
            : new String[] {"http://localhost:*", "http://127.0.0.1:*"};

    registry
        .addHandler(nativeWebSocketHandler, "/ws-native")
        .setAllowedOriginPatterns(allowedOrigins);
  }
}
