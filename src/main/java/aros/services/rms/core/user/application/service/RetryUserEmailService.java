/* (C) 2026 */

package aros.services.rms.core.user.application.service;

import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.email.application.service.EmailService;
import aros.services.rms.core.user.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.port.input.RetryUserEmailUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/** Implementation of retry registration email use case for failed email deliveries. */
@Service
public class RetryUserEmailService implements RetryUserEmailUseCase {
  private final UserRepositoryPort userPort;
  private final PasswordEncoderPort passwordEncoder;
  private final EmailService emailService;

  /**
   * Creates a retry email service.
   *
   * @param userPort repository for user operations
   * @param passwordEncoder port for password encoding
   * @param emailService service for sending emails
   */
  public RetryUserEmailService(
      UserRepositoryPort userPort,
      PasswordEncoderPort passwordEncoder,
      @Qualifier("registrationEmailUseCase") EmailService emailService) {
    this.userPort = userPort;
    this.passwordEncoder = passwordEncoder;
    this.emailService = emailService;
  }

  @Override
  public boolean retrySendRegistrationEmail(Long userId) {
    User user =
        this.userPort
            .findById(new UserId(userId))
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

    String newPassword = GenerateSecurePasswordService.execute();
    user.changePassword(this.passwordEncoder.encode(newPassword));
    this.userPort.save(user);

    boolean emailSent =
        emailService.sendRegistrationMailSync(
            user.getEmail(),
            String.format(
                "Tus credenciales de acceso. Para ingresar usa la siguiente contraseña: %s",
                newPassword));

    if (emailSent) {
      user.markAsActive();
    } else {
      user.markAsError();
    }
    this.userPort.save(user);

    return emailSent;
  }
}
