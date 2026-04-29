/* (C) 2026 */

package aros.services.rms.core.user.application.service;

import aros.services.rms.core.auth.domain.AccountSetupToken;
import aros.services.rms.core.auth.port.output.AccountSetupTokenRepositoryPort;
import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.email.port.input.WelcomeEmailUseCase;
import aros.services.rms.core.share.port.output.HashServicePort;
import aros.services.rms.core.user.application.exception.UserAlreadyExistsException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.dto.CreateUserInfo;
import aros.services.rms.core.user.port.input.CreateUserUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/** Implementation of user creation with account setup token generation and welcome email. */
public class CreateUserService implements CreateUserUseCase {
  private final UserRepositoryPort userPort;
  private final AccountSetupTokenRepositoryPort accountSetupTokenRepositoryPort;
  private final WelcomeEmailUseCase welcomeEmailUseCase;
  private final HashServicePort hashServicePort;
  private final BusinessMetricsPort metricsPort;

  /**
   * Creates a user service.
   *
   * @param userPort repository for user operations
   * @param accountSetupTokenRepositoryPort repository for account setup tokens
   * @param welcomeEmailUseCase use case for welcome emails
   * @param hashServicePort port for hashing operations
   * @param metricsPort port for business metrics
   */
  public CreateUserService(
      UserRepositoryPort userPort,
      AccountSetupTokenRepositoryPort accountSetupTokenRepositoryPort,
      WelcomeEmailUseCase welcomeEmailUseCase,
      HashServicePort hashServicePort,
      BusinessMetricsPort metricsPort) {
    this.userPort = userPort;
    this.accountSetupTokenRepositoryPort = accountSetupTokenRepositoryPort;
    this.welcomeEmailUseCase = welcomeEmailUseCase;
    this.hashServicePort = hashServicePort;
    this.metricsPort = metricsPort;
  }

  @Override
  public CreateUserResult create(CreateUserInfo info) throws UserAlreadyExistsException {
    boolean exists = this.userPort.existsByEmailOrDocument(info.document(), info.email().value());

    if (exists) {
      throw new UserAlreadyExistsException("User document or email already used");
    }

    User user =
        new User(
            null,
            info.document(),
            info.name(),
            info.email(),
            null,
            info.address(),
            info.phone(),
            UserRole.WORKER,
            UserStatus.PENDING,
            List.of());

    User saved = this.userPort.save(user);

    String rawToken = UUID.randomUUID().toString();
    String tokenHash = hashServicePort.hash(rawToken);

    Instant now = Instant.now();
    Instant expiresAt = now.plus(7, ChronoUnit.DAYS);

    AccountSetupToken token =
        new AccountSetupToken(null, saved.getId(), tokenHash, now, expiresAt, false);

    boolean emailSent = true;
    try {
      String templateName = saved.getRole() == UserRole.ADMIN ? "welcome" : "welcome_employee";
      welcomeEmailUseCase.sendWelcomeEmail(
          saved.getEmail(), rawToken, saved.getName(), templateName);
    } catch (Exception e) {
      emailSent = false;
    }

    if (!emailSent) {
      saved.markAsError();
      saved = this.userPort.save(saved);
      return new CreateUserResult(saved, null);
    }

    accountSetupTokenRepositoryPort.save(token);
    metricsPort.recordAccountSetup("requested");

    return new CreateUserResult(saved, null);
  }
}
