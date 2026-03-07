package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.dto.TwoFactorCredentials;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;

public interface VerifyTwoFactorUseCase {
    AuthResult verify(TwoFactorCredentials credentials) throws InvalidCredentialsException;
}
