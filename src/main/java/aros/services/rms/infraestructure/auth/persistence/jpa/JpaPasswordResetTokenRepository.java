/* (C) 2026 */
package aros.services.rms.infraestructure.auth.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for password reset tokens. */
public interface JpaPasswordResetTokenRepository
    extends JpaRepository<PasswordResetTokenEntity, Long> {
  /**
   * Finds a token by its hash.
   *
   * @param tokenHash the token hash
   * @return the token entity if found
   */
  Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

  /**
   * Finds a token by hash and user ID.
   *
   * @param tokenHash the token hash
   * @param userId the user ID
   * @return the token entity if found
   */
  Optional<PasswordResetTokenEntity> findByTokenHashAndUserId(String tokenHash, Long userId);

  /**
   * Deletes all tokens for a user.
   *
   * @param userId the user ID
   */
  void deleteByUserId(Long userId);
}
