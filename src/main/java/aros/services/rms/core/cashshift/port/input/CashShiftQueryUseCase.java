/* (C) 2026 */

package aros.services.rms.core.cashshift.port.input;

import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.domain.CashShiftSummary;
import java.util.Optional;

/** Read use cases for cash shifts. */
public interface CashShiftQueryUseCase {

  /**
   * Returns the currently open shift, if any.
   *
   * @return the open shift
   */
  Optional<CashShift> findCurrent();

  /**
   * Returns a shift with its reconciled totals.
   *
   * @param id the shift id
   * @return the shift summary
   */
  CashShiftSummary findSummary(Long id);
}
