/* (C) 2026 */
package aros.services.rms.config;

import aros.services.rms.core.email.application.service.EmailService;
import aros.services.rms.core.email.port.input.PasswordResetEmailUseCase;
import aros.services.rms.core.email.port.input.RegistrationEmailUseCase;
import aros.services.rms.core.email.port.input.TwoFactorAuthEmailUseCase;
import aros.services.rms.core.email.port.output.EmailServicePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailBeanConfig {

  private final EmailServicePort emailServicePort;
  private final EmailService service;

  public EmailBeanConfig(EmailServicePort emailServicePort) {
    this.emailServicePort = emailServicePort;
    this.service = new EmailService(this.emailServicePort);
  }

  @Bean
  public TwoFactorAuthEmailUseCase twoFactorAuthEmailUseCase() {
    return this.service;
  }

  @Bean
  public RegistrationEmailUseCase registrationEmailUseCase() {
    return this.service;
  }

  @Bean
  public PasswordResetEmailUseCase passwordResetEmailUseCase() {
    return this.service;
  }
}
