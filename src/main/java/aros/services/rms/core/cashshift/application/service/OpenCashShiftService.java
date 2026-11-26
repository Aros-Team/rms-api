/* (C) 2026 */

package aros.services.rms.core.cashshift.application.service;

import aros.services.rms.core.cashshift.application.dto.OpenCashShiftCommand;
import aros.services.rms.core.cashshift.application.exception.InvalidCashShiftException;
import aros.services.rms.core.cashshift.application.exception.ShiftAlreadyOpenException;
import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.domain.CashShiftStatus;
import aros.services.rms.core.cashshift.port.input.OpenCashShiftUseCase;
import aros.services.rms.core.cashshift.port.output.CashShiftRepositoryPort;
import java.time.LocalDateTime;

/** Opens a cash shift with a non-negative float; only one shift may be open at a time. */
public class OpenCashShiftService implements OpenCashShiftUseCase {

  private final CashShiftRepositoryPort cashShiftRepositoryPort;

  /**
   * Creates the service.
   *
   * @param cashShiftRepositoryPort cash-shift persistence port
   */
  public OpenCashShiftService(CashShiftRepositoryPort cashShiftRepositoryPort) {
    this.cashShiftRepositoryPort = cashShiftRepositoryPort;
  }

  /** {@inheritDoc} */
  @Override
  public CashShift open(OpenCashShiftCommand command) {
    if (command == null || command.openingFloat() == null) {
      throw new InvalidCashShiftException("El fondo inicial es obligatorio");
    }
    if (command.openingFloat().isNegative()) {
      throw new InvalidCashShiftException("El fondo inicial no puede ser negativo");
    }
    if (cashShiftRepositoryPort.findOpen().isPresent()) {
      throw new ShiftAlreadyOpenException();
    }

    CashShift shift =
        CashShift.builder()
            .status(CashShiftStatus.OPEN)
            .openingFloat(command.openingFloat())
            .openedBy(command.openedBy())
            .openedAt(LocalDateTime.now())
            .build();
    return cashShiftRepositoryPort.save(shift);
  }
}
