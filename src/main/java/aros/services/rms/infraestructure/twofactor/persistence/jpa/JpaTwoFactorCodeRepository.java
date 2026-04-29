/* (C) 2026 */
package aros.services.rms.infraestructure.twofactor.persistence.jpa;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for two-factor codes. */
public interface JpaTwoFactorCodeRepository extends JpaRepository<TwoFactorCodeEntity, Long> {
  /**
   * Finds a valid code by user ID, code hash, not used and not expired.
   *
   * @param userId the user ID
   * @param codeHash the code hash
   * @param now the current time
   * @return the code entity if found
   */
  Optional<TwoFactorCodeEntity> findByUserIdAndCodeHashAndUsedAtIsNullAndExpiresAtAfter(
      Long userId, String codeHash, Instant now);
}
