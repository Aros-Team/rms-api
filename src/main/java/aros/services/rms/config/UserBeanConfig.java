package aros.services.rms.config;

import aros.services.rms.core.auth.port.output.AccountSetupTokenRepositoryPort;
import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.email.port.input.WelcomeEmailUseCase;
import aros.services.rms.core.share.port.output.HashServicePort;
import aros.services.rms.core.user.application.service.ChangePasswordService;
import aros.services.rms.core.user.application.service.CreateUserService;
import aros.services.rms.core.user.application.service.GetAllUsersService;
import aros.services.rms.core.user.port.input.ChangePasswordUseCase;
import aros.services.rms.core.user.port.input.CreateUserUseCase;
import aros.services.rms.core.user.port.input.GetAllUsersUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring bean configuration for user-related use cases.
 *
 * <p>Wires user services with their required dependencies including repositories, email services,
 * and metrics tracking.
 */
@Configuration
public class UserBeanConfig {
  /**
   * Creates the user creation use case with all required dependencies.
   *
   * @param userPort user repository port
   * @param accountSetupTokenRepositoryPort account setup token repository
   * @param welcomeEmailUseCase welcome email use case
   * @param hashServicePort hash service for password operations
   * @param metricsPort business metrics tracking
   * @return configured CreateUserUseCase
   */
  @Bean
  public CreateUserUseCase createUserUseCase(
      UserRepositoryPort userPort,
      AccountSetupTokenRepositoryPort accountSetupTokenRepositoryPort,
      WelcomeEmailUseCase welcomeEmailUseCase,
      HashServicePort hashServicePort,
      BusinessMetricsPort metricsPort) {
    return new CreateUserService(
        userPort,
        accountSetupTokenRepositoryPort,
        welcomeEmailUseCase,
        hashServicePort,
        metricsPort);
  }

  /**
   * Creates the password change use case.
   *
   * @param userPort user repository port
   * @param passwordEncoderPort password encoder port
   * @return configured ChangePasswordUseCase
   */
  @Bean
  public ChangePasswordUseCase changePasswordUseCase(
      UserRepositoryPort userPort, PasswordEncoderPort passwordEncoderPort) {
    return new ChangePasswordService(userPort, passwordEncoderPort);
  }

  /**
   * Creates the get all users use case.
   *
   * @param userPort user repository port
   * @return configured GetAllUsersUseCase
   */
  @Bean
  public GetAllUsersUseCase getAllUsersUseCase(UserRepositoryPort userPort) {
    return new GetAllUsersService(userPort);
  }
}
