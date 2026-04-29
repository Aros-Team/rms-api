/* (C) 2026 */
package aros.services.rms.infraestructure.auth.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for refresh tokens. */
public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
  /**
   * Finds a refresh token by its hash.
   *
   * @param tokenHash the token hash
   * @return the refresh token entity if found
   */
  Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

  /**
   * Finds all refresh tokens for a user.
   *
   * @param userId the user ID
   * @return the refresh token entity if found
   */
  Optional<RefreshTokenEntity> findByUserId(Long userId);
}
