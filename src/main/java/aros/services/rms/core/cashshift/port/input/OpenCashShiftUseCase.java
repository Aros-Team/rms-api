/* (C) 2026 */

package aros.services.rms.core.cashshift.port.input;

import aros.services.rms.core.cashshift.application.dto.OpenCashShiftCommand;
import aros.services.rms.core.cashshift.domain.CashShift;

/** Use case for opening a cash shift. */
public interface OpenCashShiftUseCase {

  /**
   * Opens a new shift; only one shift may be open at a time.
   *
   * @param command the opening command
   * @return the opened shift
   */
  CashShift open(OpenCashShiftCommand command);
}
