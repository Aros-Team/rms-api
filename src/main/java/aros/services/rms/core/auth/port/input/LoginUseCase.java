/* (C) 2026 */
package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.dto.Credentials;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;

public interface LoginUseCase {
  AuthResult authenticate(Credentials credentials) throws InvalidCredentialsException;
}
