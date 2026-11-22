/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.persistence;

import aros.services.rms.core.cashshift.domain.CashShiftStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data repository for cash shifts. */
@Repository
public interface CashShiftJpaRepository extends JpaRepository<CashShiftEntity, Long> {

  /**
   * Finds the open shift, if any.
   *
   * @param status the open status
   * @return the open shift when present
   */
  Optional<CashShiftEntity> findFirstByStatus(CashShiftStatus status);
}
