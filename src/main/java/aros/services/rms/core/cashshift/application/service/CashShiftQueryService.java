/* (C) 2026 */

package aros.services.rms.core.cashshift.application.service;

import aros.services.rms.core.cashshift.application.exception.CashShiftNotFoundException;
import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.domain.CashShiftSummary;
import aros.services.rms.core.cashshift.domain.CashShiftTotals;
import aros.services.rms.core.cashshift.port.input.CashShiftQueryUseCase;
import aros.services.rms.core.cashshift.port.output.CashMovementRepositoryPort;
import aros.services.rms.core.cashshift.port.output.CashShiftRepositoryPort;
import aros.services.rms.core.payment.port.output.PaymentRepositoryPort;
import java.util.Optional;

/** Read model for the current shift and the Z-summary. */
public class CashShiftQueryService implements CashShiftQueryUseCase {

  private final CashShiftRepositoryPort cashShiftRepositoryPort;
  private final PaymentRepositoryPort paymentRepositoryPort;
  private final CashMovementRepositoryPort cashMovementRepositoryPort;

  /**
   * Creates the service.
   *
   * @param cashShiftRepositoryPort cash-shift persistence port
   * @param paymentRepositoryPort payment persistence port
   * @param cashMovementRepositoryPort movement persistence port
   */
  public CashShiftQueryService(
      CashShiftRepositoryPort cashShiftRepositoryPort,
      PaymentRepositoryPort paymentRepositoryPort,
      CashMovementRepositoryPort cashMovementRepositoryPort) {
    this.cashShiftRepositoryPort = cashShiftRepositoryPort;
    this.paymentRepositoryPort = paymentRepositoryPort;
    this.cashMovementRepositoryPort = cashMovementRepositoryPort;
  }

  /** {@inheritDoc} */
  @Override
  public Optional<CashShift> findCurrent() {
    return cashShiftRepositoryPort.findOpen();
  }

  /** {@inheritDoc} */
  @Override
  public CashShiftSummary findSummary(Long id) {
    CashShift shift =
        cashShiftRepositoryPort.findById(id).orElseThrow(() -> new CashShiftNotFoundException(id));
    CashShiftTotals totals =
        CashShiftTotals.from(
            paymentRepositoryPort.findByShiftId(shift.getId()),
            cashMovementRepositoryPort.findByShiftId(shift.getId()),
            shift.getOpeningFloat());
    return new CashShiftSummary(shift, totals);
  }
}
