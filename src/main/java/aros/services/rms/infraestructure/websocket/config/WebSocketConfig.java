package aros.services.rms.infraestructure.websocket.config;

import aros.services.rms.infraestructure.websocket.security.WebSocketChannelInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private static final String PRODUCTION = "production";

  private final WebSocketChannelInterceptor channelInterceptor;

  @Value("${app.env:development}")
  private String appEnv;

  public WebSocketConfig(WebSocketChannelInterceptor channelInterceptor) {
    this.channelInterceptor = channelInterceptor;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    boolean isProduction = PRODUCTION.equalsIgnoreCase(appEnv);
    String[] allowedOrigins =
        isProduction
            ? new String[] {"https://rms.aros.services"}
            : new String[] {"http://localhost:*", "http://127.0.0.1:*"};

    registry.addEndpoint("/ws").setAllowedOriginPatterns(allowedOrigins).withSockJS();
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(channelInterceptor);
  }
}
