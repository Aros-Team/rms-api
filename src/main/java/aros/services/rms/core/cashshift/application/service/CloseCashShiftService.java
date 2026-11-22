/* (C) 2026 */

package aros.services.rms.core.cashshift.application.service;

import aros.services.rms.core.cashshift.application.dto.CloseCashShiftCommand;
import aros.services.rms.core.cashshift.application.exception.CashShiftNotFoundException;
import aros.services.rms.core.cashshift.application.exception.InvalidCashShiftException;
import aros.services.rms.core.cashshift.application.exception.ShiftClosedException;
import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.domain.CashShiftStatus;
import aros.services.rms.core.cashshift.domain.CashShiftSummary;
import aros.services.rms.core.cashshift.domain.CashShiftTotals;
import aros.services.rms.core.cashshift.port.input.CloseCashShiftUseCase;
import aros.services.rms.core.cashshift.port.output.CashMovementRepositoryPort;
import aros.services.rms.core.cashshift.port.output.CashShiftRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.port.output.PaymentRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Closes an open shift, snapshotting expected vs counted cash on the shift row.
 *
 * <p>Totals are derived from payments (never duplicated into movements), so the Z-report stays
 * consistent with the charge history.
 */
public class CloseCashShiftService implements CloseCashShiftUseCase {

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
  public CloseCashShiftService(
      CashShiftRepositoryPort cashShiftRepositoryPort,
      PaymentRepositoryPort paymentRepositoryPort,
      CashMovementRepositoryPort cashMovementRepositoryPort) {
    this.cashShiftRepositoryPort = cashShiftRepositoryPort;
    this.paymentRepositoryPort = paymentRepositoryPort;
    this.cashMovementRepositoryPort = cashMovementRepositoryPort;
  }

  /** {@inheritDoc} */
  @Override
  public CashShiftSummary close(CloseCashShiftCommand command) {
    if (command == null || command.shiftId() == null) {
      throw new InvalidCashShiftException("A shiftId is required");
    }
    if (command.countedCash() == null) {
      throw new InvalidCashShiftException("The counted cash is required");
    }
    if (command.countedCash().isNegative()) {
      throw new InvalidCashShiftException("The counted cash cannot be negative");
    }

    CashShift shift =
        cashShiftRepositoryPort
            .findById(command.shiftId())
            .orElseThrow(() -> new CashShiftNotFoundException(command.shiftId()));
    if (shift.getStatus() != CashShiftStatus.OPEN) {
      throw new ShiftClosedException(shift.getId());
    }

    List<Payment> payments = paymentRepositoryPort.findByShiftId(shift.getId());
    CashShiftTotals totals =
        CashShiftTotals.from(
            payments,
            cashMovementRepositoryPort.findByShiftId(shift.getId()),
            shift.getOpeningFloat());

    Money counted = command.countedCash();
    Money difference = counted.minus(totals.expectedCash());
    shift.close(
        totals.expectedCash(),
        counted,
        difference,
        command.notes(),
        command.closedBy(),
        LocalDateTime.now());

    return new CashShiftSummary(cashShiftRepositoryPort.save(shift), totals);
  }
}
