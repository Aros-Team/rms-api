/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.transaction;

import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.port.input.UpdatePayrollUseCase;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional decorator for {@link UpdatePayrollUseCase}.
 *
 * <p>The partial update and the event/settlement re-aggregation share one transaction, so a failure
 * while recomputing leaves the previously persisted payroll untouched.
 */
public class TransactionalUpdatePayrollService implements UpdatePayrollUseCase {

  private final UpdatePayrollUseCase delegate;

  /**
   * Creates the decorator.
   *
   * @param delegate the framework-agnostic use case
   */
  public TransactionalUpdatePayrollService(UpdatePayrollUseCase delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public Payroll update(Long id, UpdatePayrollCommand command) {
    return delegate.update(id, command);
  }
}
