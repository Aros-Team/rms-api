/* (C) 2026 */

package aros.services.rms.core.auth.port.output;

import aros.services.rms.core.auth.domain.RefreshToken;
import aros.services.rms.core.user.domain.UserId;
import java.util.Optional;

/** Output port for refresh token persistence operations. */
public interface RefreshTokenRepositoryPort {
  /**
   * Saves a refresh token to the repository.
   *
   * @param token the refresh token to save
   * @return the saved refresh token
   */
  RefreshToken save(RefreshToken token);

  /**
   * Finds a refresh token by its hash.
   *
   * @param tokenHash the token hash to search
   * @return Optional containing the refresh token if found
   */
  Optional<RefreshToken> findByTokenHash(String tokenHash);

  /**
   * Finds all refresh tokens for a user.
   *
   * @param userId the user identifier
   * @return Optional containing the user's refresh token if found
   */
  Optional<RefreshToken> findByUserId(UserId userId);

  /**
   * Revokes a refresh token by its hash.
   *
   * @param tokenHash the token hash to revoke
   */
  void revokeByTokenHash(String tokenHash);

  /**
   * Revokes all refresh tokens for a user.
   *
   * @param userId the user identifier
   */
  void revokeAllByUserId(UserId userId);
}
