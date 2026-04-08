/* (C) 2026 */
package aros.services.rms.config;

import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.auth.application.service.GetCurrentUserService;
import aros.services.rms.core.auth.application.service.LoginService;
import aros.services.rms.core.auth.application.service.PasswordResetService;
import aros.services.rms.core.auth.application.service.RefreshTokenService;
import aros.services.rms.core.auth.application.service.VerifyTwoFactorService;
import aros.services.rms.core.auth.port.input.GetCurrentAuthUserInfoUseCase;
import aros.services.rms.core.auth.port.input.LoginUseCase;
import aros.services.rms.core.auth.port.input.PasswordResetUseCase;
import aros.services.rms.core.auth.port.input.RefreshTokensUseCase;
import aros.services.rms.core.auth.port.input.VerifyTwoFactorUseCase;
import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.auth.port.output.PasswordResetTokenRepositoryPort;
import aros.services.rms.core.auth.port.output.RefreshTokenRepositoryPort;
import aros.services.rms.core.auth.port.output.TokenPort;
import aros.services.rms.core.common.logger.Logger;
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
  private final Logger logger;

  @Bean
  public LoginUseCase loginUseCase() {
    return new LoginService(
        passwordPort,
        userPort,
        devicePort,
        tfaCodeGeneratorPort,
        tfaPort,
        emailPort,
        hashServicePort,
        refreshTokenPort,
        tokenPort);
  }

  @Bean
  public RefreshTokensUseCase refreshTokensUseCase() {
    return new RefreshTokenService(
        hashServicePort, userPort, refreshTokenPort, tokenPort, hashServicePort);
  }

  @Bean
  public VerifyTwoFactorUseCase verifyTwoFactorUseCase() {
    return new VerifyTwoFactorService(
        userPort, hashServicePort, tfaPort, devicePort, refreshTokenPort, tokenPort);
  }

  @Bean
  public PasswordResetUseCase passwordResetUseCase() {
    return new PasswordResetService(
        userPort,
        passwordResetTokenRepositoryPort,
        passwordPort,
        passwordResetEmailUseCase,
        hashServicePort,
        logger);
  }

  @Bean
  public GetCurrentAuthUserInfoUseCase getCurrentAuthUserInfoUseCase() {
    return new GetCurrentUserService(userPort, areaPort);
  }
}
