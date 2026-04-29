/* (C) 2026 */

package aros.services.rms.core.user.application.service;

import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.user.application.exception.InvalidPasswordException;
import aros.services.rms.core.user.application.exception.SamePasswordException;
import aros.services.rms.core.user.application.exception.UserNotFoundByEmailException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.input.ChangePasswordUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.util.regex.Pattern;

/** Implementation of password change use case with validation. */
public class ChangePasswordService implements ChangePasswordUseCase {

  private static final Pattern PASSWORD_PATTERN =
      Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

  private final UserRepositoryPort userRepositoryPort;
  private final PasswordEncoderPort passwordEncoderPort;

  /**
   * Creates a change password service.
   *
   * @param userRepositoryPort repository for user operations
   * @param passwordEncoderPort port for password encoding
   */
  public ChangePasswordService(
      UserRepositoryPort userRepositoryPort, PasswordEncoderPort passwordEncoderPort) {
    this.userRepositoryPort = userRepositoryPort;
    this.passwordEncoderPort = passwordEncoderPort;
  }

  @Override
  public void changePassword(String email, String currentPassword, String newPassword) {
    User user =
        userRepositoryPort.findByEmail(email).orElseThrow(() -> new UserNotFoundByEmailException());

    if (!passwordEncoderPort.validate(currentPassword, user.getPassword())) {
      throw new InvalidPasswordException("La contraseña actual no es correcta");
    }

    if (passwordEncoderPort.validate(newPassword, user.getPassword())) {
      throw new SamePasswordException();
    }

    if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
      throw new InvalidPasswordException(
          "La contraseña debe tener mínimo 8 caracteres, incluir al menos"
              + " una mayúscula, una minúscula, un número y un símbolo (@$!%*?&)");
    }

    String encodedPassword = passwordEncoderPort.encode(newPassword);
    user.changePassword(encodedPassword);
    userRepositoryPort.save(user);
  }
}
