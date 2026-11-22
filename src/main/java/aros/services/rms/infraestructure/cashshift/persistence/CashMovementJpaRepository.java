/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data repository for cash movements. */
@Repository
public interface CashMovementJpaRepository extends JpaRepository<CashMovementEntity, Long> {

  /**
   * Returns every movement of a shift.
   *
   * @param shiftId the shift id
   * @return the movements
   */
  List<CashMovementEntity> findByShiftId(Long shiftId);
}
