/* (C) 2026 */
package aros.services.rms.config;

import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.auth.application.service.AuthService;
import aros.services.rms.core.auth.application.usecases.PasswordResetUseCaseImpl;
import aros.services.rms.core.auth.port.input.PasswordResetUseCase;
import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.auth.port.output.PasswordResetTokenRepositoryPort;
import aros.services.rms.core.auth.port.output.RefreshTokenRepositoryPort;
import aros.services.rms.core.auth.port.output.TokenPort;
import aros.services.rms.core.device.port.output.DeviceRepositoryPort;
import aros.services.rms.core.email.port.input.PasswordResetEmailUseCase;
import aros.services.rms.core.email.port.input.TwoFactorAuthEmailUseCase;
import aros.services.rms.core.share.port.output.HashServicePort;
import aros.services.rms.core.twofactor.port.output.TfaCodeGeneratorPort;
import aros.services.rms.core.twofactor.port.output.TwoFactorCodeRepositoryPort;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AuthBeanConfig {

  private final UserRepositoryPort userPort;
  private final DeviceRepositoryPort devicePort;
  private final TokenPort tokenPort;
  private final PasswordEncoderPort passwordPort;
  private final TwoFactorAuthEmailUseCase emailPort;
  private final TwoFactorCodeRepositoryPort tfaPort;
  private final RefreshTokenRepositoryPort refreshTokenPort;
  private final HashServicePort hashServicePort;
  private final TfaCodeGeneratorPort tfaCodeGeneratorPort;
  private final AreaRepositoryPort areaPort;
  private final PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
  private final PasswordResetEmailUseCase passwordResetEmailUseCase;

  @Bean
  public AuthService authService() {
    return new AuthService(
        userPort,
        devicePort,
        tokenPort,
        passwordPort,
        emailPort,
        tfaPort,
        refreshTokenPort,
        hashServicePort,
        tfaCodeGeneratorPort,
        areaPort);
  }

  @Bean
  public PasswordResetUseCase passwordResetUseCase() {
    return new PasswordResetUseCaseImpl(
        userPort,
        passwordResetTokenRepositoryPort,
        passwordPort,
        passwordResetEmailUseCase,
        hashServicePort);
  }
}
