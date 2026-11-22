/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.config;

import aros.services.rms.core.cashshift.application.dto.OpenCashShiftCommand;
import aros.services.rms.core.cashshift.application.exception.ShiftAlreadyOpenException;
import aros.services.rms.core.cashshift.application.service.OpenCashShiftService;
import aros.services.rms.core.cashshift.domain.CashShift;
import aros.services.rms.core.cashshift.port.input.OpenCashShiftUseCase;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transactional entry point for opening a shift. */
@Service
@Primary
public class OpenCashShiftTransactionalService implements OpenCashShiftUseCase {

  private final OpenCashShiftService delegate;

  /**
   * Creates the wrapper.
   *
   * @param delegate the framework-free open service
   */
  public OpenCashShiftTransactionalService(OpenCashShiftService delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CashShift open(OpenCashShiftCommand command) {
    try {
      return delegate.open(command);
    } catch (DataIntegrityViolationException e) {
      if (e.getMessage() != null && e.getMessage().contains("uk_cash_shifts_open")) {
        throw new ShiftAlreadyOpenException();
      }
      throw e;
    }
  }
}
