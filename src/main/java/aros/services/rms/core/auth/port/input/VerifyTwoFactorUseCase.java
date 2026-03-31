/* (C) 2026 */
package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.dto.AuthFinalResult;
import aros.services.rms.core.auth.application.dto.TwoFactorCredentials;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;

public interface VerifyTwoFactorUseCase {
  AuthFinalResult verify(TwoFactorCredentials credentials) throws InvalidCredentialsException;
}
