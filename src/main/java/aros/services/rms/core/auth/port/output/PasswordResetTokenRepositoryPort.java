/* (C) 2026 */

package aros.services.rms.core.auth.port.output;

import aros.services.rms.core.auth.domain.PasswordResetToken;
import aros.services.rms.core.user.domain.UserId;
import java.util.Optional;

/** Output port for password reset token persistence operations. */
public interface PasswordResetTokenRepositoryPort {
  /**
   * Saves a password reset token to the repository.
   *
   * @param token the password reset token to save
   * @return the saved password reset token
   */
  PasswordResetToken save(PasswordResetToken token);

  /**
   * Finds a password reset token by hash and user.
   *
   * @param tokenHash the token hash
   * @param userId the user identifier
   * @return Optional containing the password reset token if found
   */
  Optional<PasswordResetToken> findByTokenHashAndUserId(String tokenHash, UserId userId);

  /**
   * Finds a password reset token by its hash.
   *
   * @param tokenHash the token hash
   * @return Optional containing the password reset token if found
   */
  Optional<PasswordResetToken> findByTokenHash(String tokenHash);

  /**
   * Deletes all password reset tokens for a user.
   *
   * @param userId the user identifier
   */
  void deleteByUserId(UserId userId);
}
