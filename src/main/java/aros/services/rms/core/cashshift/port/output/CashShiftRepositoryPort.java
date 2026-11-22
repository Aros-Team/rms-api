/* (C) 2026 */

package aros.services.rms.core.cashshift.port.output;

import aros.services.rms.core.cashshift.domain.CashShift;
import java.util.Optional;

/** Output port for cash-shift persistence. */
public interface CashShiftRepositoryPort {

  /**
   * Inserts or updates a shift.
   *
   * @param shift the shift to save
   * @return the saved shift
   */
  CashShift save(CashShift shift);

  /**
   * Finds a shift by id.
   *
   * @param id the shift id
   * @return the shift when present
   */
  Optional<CashShift> findById(Long id);

  /**
   * Returns the currently open shift.
   *
   * @return the open shift when present
   */
  Optional<CashShift> findOpen();
}
