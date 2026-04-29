/* (C) 2026 */

package aros.services.rms.core.auth.port.output;

import aros.services.rms.core.user.domain.User;

/** Output port for JWT token generation operations. */
public interface TokenPort {
  /**
   * Generates an access token for a user.
   *
   * @param user the user to generate token for
   * @return the access token string
   */
  String generateAccessToken(User user);

  /**
   * Generates a two-factor authentication token for a user.
   *
   * @param user the user to generate token for
   * @return the TFA token string
   */
  String generateTfaToken(User user);

  /**
   * Generates a refresh token for a user.
   *
   * @param user the user to generate token for
   * @return the refresh token string
   */
  String generateRefreshToken(User user);
}
