/* (C) 2026 */
package aros.services.rms.core.auth.application.usecases;

import aros.services.rms.core.auth.application.exception.PasswordResetTokenExpiredException;
import aros.services.rms.core.auth.application.exception.PasswordResetTokenInvalidException;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.user.application.exception.InvalidPasswordException;
import aros.services.rms.core.auth.domain.PasswordResetToken;
import aros.services.rms.core.auth.port.input.PasswordResetUseCase;
import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.auth.port.output.PasswordResetTokenRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.email.port.input.PasswordResetEmailUseCase;
import aros.services.rms.core.share.port.output.HashServicePort;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.regex.Pattern;

public class PasswordResetUseCaseImpl implements PasswordResetUseCase {

  private static final int TOKEN_EXPIRATION_MINUTES = 30;

  private static final Pattern PASSWORD_PATTERN =
      Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

  private final UserRepositoryPort userRepositoryPort;
  private final PasswordResetTokenRepositoryPort tokenRepositoryPort;
  private final PasswordEncoderPort passwordEncoderPort;
  private final PasswordResetEmailUseCase emailUseCase;
  private final HashServicePort hashServicePort;
  private final Logger logger;

  public PasswordResetUseCaseImpl(
      UserRepositoryPort userRepositoryPort,
      PasswordResetTokenRepositoryPort tokenRepositoryPort,
      PasswordEncoderPort passwordEncoderPort,
      PasswordResetEmailUseCase emailUseCase,
      HashServicePort hashServicePort,
      Logger logger) {
    this.userRepositoryPort = userRepositoryPort;
    this.tokenRepositoryPort = tokenRepositoryPort;
    this.passwordEncoderPort = passwordEncoderPort;
    this.emailUseCase = emailUseCase;
    this.hashServicePort = hashServicePort;
    this.logger = logger;
  }

  @Override
  public void requestPasswordReset(String email) throws UserNotFoundException {
    logger.info("Password reset requested: email={}", email);

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

    logger.info("Password reset token generated: userId={}, expiresAt={}", user.getId(), expiresAt);

    emailUseCase.sendPasswordResetEmail(user.getEmail(), rawToken);
  }

  @Override
  public void resetPassword(String token, String newPassword) {
    logger.info("Password reset attempt started");

    String tokenHash = hashServicePort.hash(token);

    PasswordResetToken resetToken =
        tokenRepositoryPort
            .findByTokenHash(tokenHash)
            .orElseThrow(
                () ->
                    new PasswordResetTokenInvalidException(
                        "El token de recuperación no es válido"));

    if (resetToken.used()) {
      throw new PasswordResetTokenInvalidException(
          "El token de recuperación ya ha sido utilizado. Solicita uno nuevo");
    }

    if (resetToken.isExpired()) {
      throw new PasswordResetTokenExpiredException(
          "El token de recuperación ha expirado. Solicita uno nuevo");
    }

    User user =
        userRepositoryPort
            .findById(resetToken.userId())
            .orElseThrow(
                () ->
                    new PasswordResetTokenInvalidException(
                        "El token de recuperación no es válido"));

    if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
      throw new InvalidPasswordException(
          "La nueva contraseña debe tener mínimo 8 caracteres, incluir al menos una mayúscula, una minúscula, un número y un símbolo (@$!%*?&)");
    }

    String encodedPassword = passwordEncoderPort.encode(newPassword);
    user.changePassword(encodedPassword);
    userRepositoryPort.save(user);

    PasswordResetToken usedToken = resetToken.markAsUsed();
    tokenRepositoryPort.save(usedToken);

    logger.info("Password changed successfully: userId={}", user.getId());
  }
}