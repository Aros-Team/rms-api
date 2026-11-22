/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.config;

import aros.services.rms.core.cashshift.application.dto.CloseCashShiftCommand;
import aros.services.rms.core.cashshift.application.service.CloseCashShiftService;
import aros.services.rms.core.cashshift.domain.CashShiftSummary;
import aros.services.rms.core.cashshift.port.input.CloseCashShiftUseCase;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transactional entry point for closing a shift. */
@Service
@Primary
public class CloseCashShiftTransactionalService implements CloseCashShiftUseCase {

  private final CloseCashShiftService delegate;

  /**
   * Creates the wrapper.
   *
   * @param delegate the framework-free close service
   */
  public CloseCashShiftTransactionalService(CloseCashShiftService delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CashShiftSummary close(CloseCashShiftCommand command) {
    return delegate.close(command);
  }
}
