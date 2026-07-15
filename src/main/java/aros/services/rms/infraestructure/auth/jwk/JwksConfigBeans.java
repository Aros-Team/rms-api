/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwk;

import aros.services.rms.core.auth.application.service.PublishJwksService;
import aros.services.rms.core.auth.domain.jwk.port.input.PublishJwksUseCase;
import aros.services.rms.core.auth.domain.jwk.port.output.JwkSourcePort;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for JWKS module. Registers cache configuration properties and use case beans. */
@Configuration
@EnableConfigurationProperties(JwksCacheProperties.class)
public class JwksConfigBeans {

  /** Registers use case bean for JWKS publishing. */
  @Bean
  public PublishJwksUseCase publishJwksUseCase(JwkSourcePort jwkSourcePort) {
    return new PublishJwksService(jwkSourcePort);
  }
}
