/* (C) 2026 */
package aros.services.rms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import aros.services.rms.core.auth.application.service.AuthService;
import aros.services.rms.core.auth.port.input.LoginUseCase;
import aros.services.rms.core.auth.port.input.VerifyTwoFactorUseCase;
import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.auth.port.output.RefreshTokenRepositoryPort;
import aros.services.rms.core.auth.port.output.TokenPort;
import aros.services.rms.core.device.port.output.DeviceRepositoryPort;
import aros.services.rms.core.email.port.input.TwoFactorAuthEmailUseCase;
import aros.services.rms.core.share.port.output.HashServicePort;
import aros.services.rms.core.twofactor.port.output.TfaCodeGeneratorPort;
import aros.services.rms.core.twofactor.port.output.TwoFactorCodeRepositoryPort;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

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

    @Bean
    public LoginUseCase loginUseCase() {
        return new AuthService(
            userPort,
            devicePort,
            tokenPort,
            passwordPort,
            emailPort,
            tfaPort,
            refreshTokenPort,
            hashServicePort,
            tfaCodeGeneratorPort
        );
    }

    @Bean
    public VerifyTwoFactorUseCase verifyTwoFactorUseCase() {
        return new AuthService(
            userPort,
            devicePort,
            tokenPort,
            passwordPort,
            emailPort,
            tfaPort,
            refreshTokenPort,
            hashServicePort,
            tfaCodeGeneratorPort
        );
    }
}
