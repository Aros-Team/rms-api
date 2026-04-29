/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwt;

import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.user.domain.UserRole;
import java.util.List;

/** Service for JWT token operations. */
public interface JwtService {
  /**
   * Generates an access token.
   *
   * @param username the username
   * @param role the user role
   * @param areas the list of area IDs
   * @return the access token
   */
  String generateAccessToken(String username, UserRole role, List<AreaId> areas);

  /**
   * Generates a refresh token.
   *
   * @param username the username
   * @return the refresh token
   */
  String generateRefreshToken(String username);

  /**
   * Generates a two-factor authentication token.
   *
   * @param username the username
   * @return the TFA token
   */
  String generateTfaToken(String username);

  /**
   * Validates a JWT token.
   *
   * @param token the token to validate
   * @return true if valid, false otherwise
   */
  boolean validateToken(String token);

  /**
   * Extracts the username from a token.
   *
   * @param token the token
   * @return the username
   */
  String getUsernameFromToken(String token);

  /**
   * Extracts the role from a token.
   *
   * @param token the token
   * @return the user role
   */
  UserRole getRoleFromToken(String token);

  /**
   * Extracts the areas from a token.
   *
   * @param token the token
   * @return the list of area IDs
   */
  List<AreaId> getAreasFromToken(String token);
}
