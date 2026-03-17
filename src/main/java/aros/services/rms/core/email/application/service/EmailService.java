/* (C) 2026 */
package aros.services.rms.core.email.application.service;

import aros.services.rms.core.email.domain.Email;
import aros.services.rms.core.email.port.input.PasswordResetEmailUseCase;
import aros.services.rms.core.email.port.input.RegistrationEmailUseCase;
import aros.services.rms.core.email.port.input.TwoFactorAuthEmailUseCase;
import aros.services.rms.core.email.port.output.EmailServicePort;
import aros.services.rms.core.user.domain.UserEmail;
import java.util.Map;

public class EmailService
    implements TwoFactorAuthEmailUseCase, RegistrationEmailUseCase, PasswordResetEmailUseCase {
  private final EmailServicePort emailPort;

  public EmailService(EmailServicePort emailPort) {
    this.emailPort = emailPort;
  }

  @Override
  public void sendTwoFactorCode(UserEmail email, String code) {
    Email tfaEmail =
        new Email(
            email.value(),
            "admin@aros.service",
            "two_factor",
            Map.of("code", code, "expiry", "5 minutos"));

    this.emailPort.send(tfaEmail);
  }

  @Override
  public void sendRegistrationMail(UserEmail destination, String message) {
    Email regEmail =
        new Email(
            destination.value(), "admin@aros.service", "notification", Map.of("message", message));

    this.emailPort.send(regEmail);
  }

  @Override
  public void sendPasswordResetEmail(UserEmail destination, String resetToken) {
    Email resetEmail =
        new Email(
            destination.value(),
            "admin@aros.service",
            "notification",
            // Map.of("token", resetToken, "expiry", "30 minutos")
            Map.of("message", "Token de verificacion: " + resetToken));

    this.emailPort.send(resetEmail);
  }
}
