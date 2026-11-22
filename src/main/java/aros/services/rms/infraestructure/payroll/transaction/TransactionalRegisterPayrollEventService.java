/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.transaction;

import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.port.input.RegisterPayrollEventUseCase;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional decorator for {@link RegisterPayrollEventUseCase}.
 *
 * <p>Wraps the insert of the event row and the payroll re-aggregation in one unit of work, so a
 * failure after the insert cannot leave an event that is not reflected in the payroll totals.
 */
public class TransactionalRegisterPayrollEventService implements RegisterPayrollEventUseCase {

  private final RegisterPayrollEventUseCase delegate;

  /**
   * Creates the decorator.
   *
   * @param delegate the framework-agnostic use case
   */
  public TransactionalRegisterPayrollEventService(RegisterPayrollEventUseCase delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public PayrollEvent execute(PayrollEvent event) {
    return delegate.execute(event);
  }
}
