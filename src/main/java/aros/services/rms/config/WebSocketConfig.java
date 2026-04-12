/* (C) 2026 */
package aros.services.rms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración del broker de mensajes WebSocket con STOMP. Expone el endpoint /ws para conexiones
 * WebSocket y configura el broker en memoria para los topics de órdenes.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // Habilita un broker en memoria para los destinos con prefijo /topic
    registry.enableSimpleBroker("/topic");
    // Prefijo para mensajes enviados desde el cliente al servidor
    registry.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Endpoint principal WebSocket — el frontend se conecta a ws(s)://{host}/ws
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
  }
}
