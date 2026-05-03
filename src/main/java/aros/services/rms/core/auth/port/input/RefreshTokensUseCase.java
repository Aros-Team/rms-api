/* (C) 2026 */

package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.exception.InvalidRefreshTokenException;

/** Input port for refresh token operations. */
public interface RefreshTokensUseCase {
  /**
   * Refreshes authentication tokens using a refresh token.
   *
   * @param refreshTokenHash the hashed refresh token
   * @return new authentication result with fresh tokens
   * @throws InvalidRefreshTokenException if token is invalid or expired
   */
  AuthResult refresh(String refreshTokenHash) throws InvalidRefreshTokenException;
}
