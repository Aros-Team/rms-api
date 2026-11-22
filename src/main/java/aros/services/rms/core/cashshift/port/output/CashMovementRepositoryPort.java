/* (C) 2026 */

package aros.services.rms.core.cashshift.port.output;

import aros.services.rms.core.cashshift.domain.CashMovement;
import java.util.List;

/** Output port for the manual cash-movement ledger (read side used by the Z totals). */
public interface CashMovementRepositoryPort {

  /**
   * Returns every movement of a shift.
   *
   * @param shiftId the shift id
   * @return the movements
   */
  List<CashMovement> findByShiftId(Long shiftId);
}
