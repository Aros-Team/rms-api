/* (C) 2026 */
package aros.services.rms.core.auth.application.service;

import aros.services.rms.core.auth.application.exception.PasswordResetTokenExpiredException;
import aros.services.rms.core.auth.application.exception.PasswordResetTokenInvalidException;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.auth.domain.PasswordResetToken;
import aros.services.rms.core.auth.port.input.PasswordResetUseCase;
import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.auth.port.output.PasswordResetTokenRepositoryPort;
import aros.services.rms.core.email.port.input.PasswordResetEmailUseCase;
import aros.services.rms.core.share.port.output.HashServicePort;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class PasswordResetService implements PasswordResetUseCase {

  private static final int TOKEN_EXPIRATION_MINUTES = 30;

  private final UserRepositoryPort userRepositoryPort;
  private final PasswordResetTokenRepositoryPort tokenRepositoryPort;
  private final PasswordEncoderPort passwordEncoderPort;
  private final PasswordResetEmailUseCase emailUseCase;
  private final HashServicePort hashServicePort;

  public PasswordResetService(
      UserRepositoryPort userRepositoryPort,
      PasswordResetTokenRepositoryPort tokenRepositoryPort,
      PasswordEncoderPort passwordEncoderPort,
      PasswordResetEmailUseCase emailUseCase,
      HashServicePort hashServicePort) {
    this.userRepositoryPort = userRepositoryPort;
    this.tokenRepositoryPort = tokenRepositoryPort;
    this.passwordEncoderPort = passwordEncoderPort;
    this.emailUseCase = emailUseCase;
    this.hashServicePort = hashServicePort;
  }

  @Override
  public void requestPasswordReset(String email) throws UserNotFoundException {
    User user =
        userRepositoryPort.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

    tokenRepositoryPort.deleteByUserId(user.getId());

    String rawToken = UUID.randomUUID().toString();
    String tokenHash = hashServicePort.hash(rawToken);

    Instant now = Instant.now();
    Instant expiresAt = now.plus(TOKEN_EXPIRATION_MINUTES, ChronoUnit.MINUTES);

    PasswordResetToken token =
        new PasswordResetToken(null, user.getId(), tokenHash, now, expiresAt, false);

    tokenRepositoryPort.save(token);

    emailUseCase.sendPasswordResetEmail(user.getEmail(), rawToken);
  }

  @Override
  public void resetPassword(String token, String newPassword) {
    String tokenHash = hashServicePort.hash(token);

    PasswordResetToken resetToken =
        tokenRepositoryPort
            .findByTokenHash(tokenHash)
            .orElseThrow(PasswordResetTokenInvalidException::new);

    if (resetToken.used()) {
      throw new PasswordResetTokenInvalidException("El token ya ha sido utilizado");
    }

    if (resetToken.isExpired()) {
      throw new PasswordResetTokenExpiredException();
    }

    User user =
        userRepositoryPort
            .findById(resetToken.userId())
            .orElseThrow(PasswordResetTokenInvalidException::new);

    String encodedPassword = passwordEncoderPort.encode(newPassword);
    user.changePassword(encodedPassword);
    userRepositoryPort.save(user);

    PasswordResetToken usedToken = resetToken.markAsUsed();
    tokenRepositoryPort.save(usedToken);
  }
}
