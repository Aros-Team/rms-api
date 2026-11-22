/* (C) 2026 */

package aros.services.rms.infraestructure.twofactor.persistence.jpa;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for two-factor codes. */
public interface JpaTwoFactorCodeRepository extends JpaRepository<TwoFactorCodeEntity, Long> {
  /**
   * Finds the newest valid code by user ID, code hash, not used and not expired.
   *
   * <p>A user can legitimately have more than one unused code with the same value (a repeated
   * six-digit code, or a retry after an email failure). The query must therefore return the latest
   * match instead of failing with a non-unique result.
   *
   * @param userId the user ID
   * @param codeHash the code hash
   * @param now the current time
   * @return the newest matching code entity if found
   */
  Optional<TwoFactorCodeEntity>
      findFirstByUserIdAndCodeHashAndUsedAtIsNullAndExpiresAtAfterOrderByIdDesc(
          Long userId, String codeHash, Instant now);
}
