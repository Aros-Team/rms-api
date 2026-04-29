/* (C) 2026 */
package aros.services.rms.infraestructure.auth.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for account setup tokens. */
public interface JpaAccountSetupTokenRepository
    extends JpaRepository<AccountSetupTokenEntity, Long> {
  /**
   * Finds a token by its hash.
   *
   * @param tokenHash the token hash
   * @return the token entity if found
   */
  Optional<AccountSetupTokenEntity> findByTokenHash(String tokenHash);

  /**
   * Finds a token by hash and user ID.
   *
   * @param tokenHash the token hash
   * @param userId the user ID
   * @return the token entity if found
   */
  Optional<AccountSetupTokenEntity> findByTokenHashAndUserId(String tokenHash, Long userId);

  /**
   * Deletes all tokens for a user.
   *
   * @param userId the user ID
   */
  void deleteByUserId(Long userId);
}
