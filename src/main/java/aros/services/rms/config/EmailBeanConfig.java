/* (C) 2026 */
package aros.services.rms.config;

import aros.services.rms.core.email.application.service.EmailService;
import aros.services.rms.core.email.port.input.TwoFactorAuthEmailUseCase;
import aros.services.rms.core.email.port.output.EmailServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class EmailBeanConfig {

  private final EmailServicePort emailServicePort;

  @Bean
  public TwoFactorAuthEmailUseCase twoFactorAuthEmailUseCase() {
    return new EmailService(emailServicePort);
  }
}
