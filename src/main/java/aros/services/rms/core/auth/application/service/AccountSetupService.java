/* (C) 2026 */

package aros.services.rms.core.auth.application.service;

import aros.services.rms.core.auth.application.exception.AccountSetupTokenAlreadyUsedException;
import aros.services.rms.core.auth.application.exception.AccountSetupTokenExpiredException;
import aros.services.rms.core.auth.application.exception.AccountSetupTokenInvalidException;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.auth.domain.AccountSetupToken;
import aros.services.rms.core.auth.port.input.AccountSetupUseCase;
import aros.services.rms.core.auth.port.output.AccountSetupTokenRepositoryPort;
import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.email.port.input.WelcomeEmailUseCase;
import aros.services.rms.core.share.port.output.HashServicePort;
import aros.services.rms.core.user.application.exception.InvalidPasswordException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.auth.api.dto.SetupAccountValidationResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.regex.Pattern;

/** Service for handling new user account setup with token validation. */
public class AccountSetupService implements AccountSetupUseCase {

  private static final int TOKEN_EXPIRATION_MINUTES = 30;

  private static final Pattern PASSWORD_PATTERN =
      Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

  private final UserRepositoryPort userRepositoryPort;
  private final AccountSetupTokenRepositoryPort tokenRepositoryPort;
  private final PasswordEncoderPort passwordEncoderPort;
  private final HashServicePort hashServicePort;
  private final Logger logger;
  private final BusinessMetricsPort metricsPort;
  private final WelcomeEmailUseCase welcomeEmailUseCase;

  /**
   * Creates a new AccountSetupService instance.
   *
   * @param userRepositoryPort the user repository port
   * @param tokenRepositoryPort the account setup token repository port
   * @param passwordEncoderPort the password encoder port
   * @param hashServicePort the hash service port
   * @param logger the logger instance
   * @param metricsPort the business metrics port
   * @param welcomeEmailUseCase the welcome email use case
   */
  public AccountSetupService(
      UserRepositoryPort userRepositoryPort,
      AccountSetupTokenRepositoryPort tokenRepositoryPort,
      PasswordEncoderPort passwordEncoderPort,
      HashServicePort hashServicePort,
      Logger logger,
      BusinessMetricsPort metricsPort,
      WelcomeEmailUseCase welcomeEmailUseCase) {
    this.userRepositoryPort = userRepositoryPort;
    this.tokenRepositoryPort = tokenRepositoryPort;
    this.passwordEncoderPort = passwordEncoderPort;
    this.hashServicePort = hashServicePort;
    this.logger = logger;
    this.metricsPort = metricsPort;
    this.welcomeEmailUseCase = welcomeEmailUseCase;
  }

  /**
   * Sets up a new user's password using an account setup token.
   *
   * @param token the account setup token
   * @param newPassword the new password to set
   * @param name the user's name
   * @param document the user's document number
   */
  @Override
  public void setupPassword(String token, String newPassword, String name, String document) {
    logger.info("Account setup password attempt started");

    String tokenHash = hashServicePort.hash(token);

    AccountSetupToken setupToken =
        tokenRepositoryPort
            .findByTokenHash(tokenHash)
            .orElseThrow(
                () -> new AccountSetupTokenInvalidException("Token de configuración no válido"));

    if (setupToken.used()) {
      throw new AccountSetupTokenAlreadyUsedException(
          "Token de configuración ya ha sido utilizado. Solicita uno nuevo");
    }

    if (setupToken.isExpired()) {
      throw new AccountSetupTokenExpiredException(
          "Token de configuración expirado. Solicita uno nuevo");
    }

    User user =
        userRepositoryPort
            .findById(setupToken.userId())
            .orElseThrow(
                () -> new AccountSetupTokenInvalidException("Token de configuración no válido"));

    // If user is ADMIN, validate name and document are provided
    if (user.getRole() == UserRole.ADMIN) {
      if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("Name is required for admin users");
      }
      if (document == null || document.isBlank()) {
        throw new IllegalArgumentException("Document is required for admin users");
      }
      if (!user.getDocument().equals(document)) {
        throw new AccountSetupTokenInvalidException("Documento no válido para este token");
      }
      // Update name if different
      if (!user.getName().equals(name)) {
        user.updateName(name);
      }
    }

    if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
      throw new InvalidPasswordException(
          "La nueva contraseña debe tener mínimo 8 caracteres, incluir al menos"
              + " una mayúscula, una minúscula, un número y un símbolo (@$!%*?&)");
    }

    String encodedPassword = passwordEncoderPort.encode(newPassword);

    AccountSetupToken usedToken = setupToken.markAsUsed();
    tokenRepositoryPort.save(usedToken);

    user.changePassword(encodedPassword);
    user.markAsActive();
    userRepositoryPort.save(user);

    logger.info("Account setup completed successfully: userId={}", user.getId());
    metricsPort.recordAccountSetup("completed");
  }

  /**
   * Requests an account setup email for a new user.
   *
   * @param email the user's email address
   * @throws UserNotFoundException if no user exists with that email
   */
  @Override
  public void requestSetupEmail(String email) throws UserNotFoundException {
    logger.info("Account setup email requested: email={}", email);

    User user =
        userRepositoryPort
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new UserNotFoundException(
                        "the user with email " + email + " couldn't be found"));

    tokenRepositoryPort.deleteByUserId(user.getId());

    String rawToken = UUID.randomUUID().toString();
    String tokenHash = hashServicePort.hash(rawToken);

    Instant now = Instant.now();
    Instant expiresAt = now.plus(TOKEN_EXPIRATION_MINUTES, ChronoUnit.MINUTES);

    AccountSetupToken token =
        new AccountSetupToken(null, user.getId(), tokenHash, now, expiresAt, false);

    tokenRepositoryPort.save(token);

    logger.info("Account setup token generated: userId={}, expiresAt={}", user.getId(), expiresAt);

    String templateName = user.getRole() == UserRole.ADMIN ? "welcome" : "welcome_employee";
    welcomeEmailUseCase.sendWelcomeEmail(user.getEmail(), rawToken, user.getName(), templateName);

    metricsPort.recordAccountSetup("requested");
  }

  /**
   * Deletes all existing account setup tokens for a user.
   *
   * @param userId the user identifier
   */
  @Override
  public void deleteExistingTokens(UserId userId) {
    logger.info("Deleting existing setup tokens: userId={}", userId);
    tokenRepositoryPort.deleteByUserId(userId);
  }

  /**
   * Validates an account setup token.
   *
   * @param token the token to validate
   * @return validation response with token status
   * @throws AccountSetupTokenInvalidException if token is invalid
   * @throws AccountSetupTokenExpiredException if token has expired
   */
  @Override
  public SetupAccountValidationResponse validateToken(String token) {
    String tokenHash = hashServicePort.hash(token);

    AccountSetupToken setupToken =
        tokenRepositoryPort
            .findByTokenHash(tokenHash)
            .orElseThrow(
                () -> new AccountSetupTokenInvalidException("Token de configuración no válido"));

    if (setupToken.isExpired()) {
      throw new AccountSetupTokenExpiredException("Token de configuración expirado");
    }

    User user =
        userRepositoryPort
            .findById(setupToken.userId())
            .orElseThrow(
                () -> new AccountSetupTokenInvalidException("Token de configuración no válido"));

    return SetupAccountValidationResponse.fromDomain(user);
  }
}
