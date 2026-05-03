/* (C) 2026 */

package aros.services.rms.config;

import aros.services.rms.core.email.application.service.EmailService;
import aros.services.rms.core.email.port.input.PasswordResetEmailUseCase;
import aros.services.rms.core.email.port.input.RegistrationEmailUseCase;
import aros.services.rms.core.email.port.input.TwoFactorAuthEmailUseCase;
import aros.services.rms.core.email.port.input.WelcomeEmailUseCase;
import aros.services.rms.core.email.port.output.EmailServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for email use case beans.
 *
 * <p>Binds the EmailService implementation to the various email input ports.
 */
@Configuration
public class EmailBeanConfig {

  private final EmailServicePort emailServicePort;
  private final EmailService service;

  /** Constructs EmailBeanConfig with required dependencies. */
  public EmailBeanConfig(
      EmailServicePort emailServicePort, @Value("${app.env:development}") String appEnv) {
    this.emailServicePort = emailServicePort;
    this.service = new EmailService(this.emailServicePort, appEnv);
  }

  /** Returns the TwoFactorAuthEmailUseCase implementation. */
  @Bean
  public TwoFactorAuthEmailUseCase twoFactorAuthEmailUseCase() {
    return this.service;
  }

  /** Returns the RegistrationEmailUseCase implementation. */
  @Bean
  public RegistrationEmailUseCase registrationEmailUseCase() {
    return this.service;
  }

  /** Returns the PasswordResetEmailUseCase implementation. */
  @Bean
  public PasswordResetEmailUseCase passwordResetEmailUseCase() {
    return this.service;
  }

  /** Returns the WelcomeEmailUseCase implementation. */
  @Bean
  public WelcomeEmailUseCase welcomeEmailUseCase() {
    return this.service;
  }
}
