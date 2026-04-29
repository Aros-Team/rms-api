/* (C) 2026 */

package aros.services.rms.core.email.application.service;

import aros.services.rms.core.email.domain.Email;
import aros.services.rms.core.email.port.input.PasswordResetEmailUseCase;
import aros.services.rms.core.email.port.input.RegistrationEmailUseCase;
import aros.services.rms.core.email.port.input.TwoFactorAuthEmailUseCase;
import aros.services.rms.core.email.port.input.WelcomeEmailUseCase;
import aros.services.rms.core.email.port.output.EmailServicePort;
import aros.services.rms.core.user.domain.UserEmail;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

/** Implementation of email sending services for authentication workflows. */
public class EmailService
    implements TwoFactorAuthEmailUseCase,
        RegistrationEmailUseCase,
        PasswordResetEmailUseCase,
        WelcomeEmailUseCase {

  private static final String UI_PRODUCTION = "https://rms.aros.services";
  private static final String UI_DEVELOPMENT = "http://localhost:4200";

  private final EmailServicePort emailPort;
  private final String uiBaseUrl;

  /**
   * Creates an email service.
   *
   * @param emailPort port for sending emails
   * @param appEnv application environment
   */
  public EmailService(EmailServicePort emailPort, @Value("${app.env:development}") String appEnv) {
    this.emailPort = emailPort;
    this.uiBaseUrl = "production".equalsIgnoreCase(appEnv) ? UI_PRODUCTION : UI_DEVELOPMENT;
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

  /**
   * Sends registration email synchronously.
   *
   * @param destination recipient email
   * @param message the message content
   * @return true if email was sent successfully
   */
  public boolean sendRegistrationMailSync(UserEmail destination, String message) {
    Email regEmail =
        new Email(
            destination.value(), "admin@aros.service", "notification", Map.of("message", message));

    return this.emailPort.sendSync(regEmail);
  }

  @Override
  public void sendPasswordResetEmail(UserEmail destination, String resetToken) {
    String resetLink = uiBaseUrl + "/reset-password?token=" + resetToken;

    Email resetEmail =
        new Email(
            destination.value(),
            "admin@aros.service",
            "password_reset",
            Map.of(
                "reset_link",
                resetLink,
                "expiry",
                "30 minutos",
                "message",
                "Has solicitado restablecer tu contraseña en RMS. Haz clic en el siguiente enlace para completar el proceso:"));

    this.emailPort.send(resetEmail);
  }

  @Override
  public void sendWelcomeEmail(
      UserEmail destination, String welcomeToken, String userName, String templateName) {
    String welcomeLink = uiBaseUrl + "/setup-account?token=" + welcomeToken;
    Email welcomeEmail =
        new Email(
            destination.value(),
            "admin@aros.service",
            templateName,
            Map.of("name", userName, "welcome_link", welcomeLink, "expiry", "30 minutos"));
    this.emailPort.send(welcomeEmail);
  }
}
