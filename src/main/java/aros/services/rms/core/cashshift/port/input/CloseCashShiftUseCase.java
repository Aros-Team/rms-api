/* (C) 2026 */

package aros.services.rms.core.cashshift.port.input;

import aros.services.rms.core.cashshift.application.dto.CloseCashShiftCommand;
import aros.services.rms.core.cashshift.domain.CashShiftSummary;

/** Use case for closing a cash shift and snapshotting the reconciliation. */
public interface CloseCashShiftUseCase {

  /**
   * Closes an open shift with the counted cash.
   *
   * @param command the closing command
   * @return the closed shift with its totals
   */
  CashShiftSummary close(CloseCashShiftCommand command);
}
