/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.transaction;

import aros.services.rms.core.payroll.domain.port.input.DeletePayrollEventUseCase;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional decorator for {@link DeletePayrollEventUseCase}.
 *
 * <p>Deletes the event row and re-aggregates the payroll in one unit of work, so a failure cannot
 * remove an event while leaving the payroll totals unchanged.
 */
public class TransactionalDeletePayrollEventService implements DeletePayrollEventUseCase {

  private final DeletePayrollEventUseCase delegate;

  /**
   * Creates the decorator.
   *
   * @param delegate the framework-agnostic use case
   */
  public TransactionalDeletePayrollEventService(DeletePayrollEventUseCase delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void execute(Long eventId) {
    delegate.execute(eventId);
  }
}
