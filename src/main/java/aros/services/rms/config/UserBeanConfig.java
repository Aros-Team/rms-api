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

@Configuration
public class UserBeanConfig {
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

  @Bean
  public ChangePasswordUseCase changePasswordUseCase(
      UserRepositoryPort userPort, PasswordEncoderPort passwordEncoderPort) {
    return new ChangePasswordService(userPort, passwordEncoderPort);
  }

  @Bean
  public GetAllUsersUseCase getAllUsersUseCase(UserRepositoryPort userPort) {
    return new GetAllUsersService(userPort);
  }
}
