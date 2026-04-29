/* (C) 2026 */

package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.dto.Credentials;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;

/** Input port for user login operations. */
public interface LoginUseCase {
  /**
   * Authenticates a user with credentials.
   *
   * @param credentials the user's login credentials
   * @return the authentication result (success, TFA required, or failure)
   * @throws InvalidCredentialsException if credentials are invalid
   */
  AuthResult authenticate(Credentials credentials) throws InvalidCredentialsException;
}
