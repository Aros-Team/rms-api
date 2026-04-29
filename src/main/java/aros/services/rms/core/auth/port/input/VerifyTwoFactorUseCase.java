/* (C) 2026 */

package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.dto.TwoFactorCredentials;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;

/** Input port for two-factor verification operations. */
public interface VerifyTwoFactorUseCase {
  /**
   * Verifies a two-factor authentication code.
   *
   * @param credentials the 2FA credentials including username and code
   * @return the authentication result if code is valid
   * @throws InvalidCredentialsException if code is invalid or expired
   */
  AuthResult verify(TwoFactorCredentials credentials) throws InvalidCredentialsException;
}
