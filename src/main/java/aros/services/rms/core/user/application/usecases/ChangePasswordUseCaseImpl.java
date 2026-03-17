/* (C) 2026 */
package aros.services.rms.core.user.application.usecases;

import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.user.application.exception.InvalidPasswordException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.input.ChangePasswordUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;

public class ChangePasswordUseCaseImpl implements ChangePasswordUseCase {

  private final UserRepositoryPort userRepositoryPort;
  private final PasswordEncoderPort passwordEncoderPort;

  public ChangePasswordUseCaseImpl(
      UserRepositoryPort userRepositoryPort, PasswordEncoderPort passwordEncoderPort) {
    this.userRepositoryPort = userRepositoryPort;
    this.passwordEncoderPort = passwordEncoderPort;
  }

  @Override
  public void changePassword(String email, String currentPassword, String newPassword) {
    User user =
        userRepositoryPort
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    if (!passwordEncoderPort.validate(currentPassword, user.getPassword())) {
      throw new InvalidPasswordException();
    }

    String encodedPassword = passwordEncoderPort.encode(newPassword);
    user.changePassword(encodedPassword);
    userRepositoryPort.save(user);
  }
}
