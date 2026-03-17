package aros.services.rms.config;

import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.email.port.input.RegistrationEmailUseCase;
import aros.services.rms.core.share.port.output.HashServicePort;
import aros.services.rms.core.user.application.usecases.ChangePasswordUseCaseImpl;
import aros.services.rms.core.user.application.usecases.CreateUserService;
import aros.services.rms.core.user.port.input.ChangePasswordUseCase;
import aros.services.rms.core.user.port.input.CreateUserUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserBeanConfig {
  @Bean
  public CreateUserUseCase createUserUseCase(
      UserRepositoryPort userPort,
      HashServicePort hashPort,
      PasswordEncoderPort passwordPort,
      RegistrationEmailUseCase registrationEmailUseCase) {
    return new CreateUserService(userPort, hashPort, registrationEmailUseCase, passwordPort);
  }

  @Bean
  public ChangePasswordUseCase changePasswordUseCase(
      UserRepositoryPort userPort, PasswordEncoderPort passwordEncoderPort) {
    return new ChangePasswordUseCaseImpl(userPort, passwordEncoderPort);
  }
}
