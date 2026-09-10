/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.input;

import aros.services.rms.core.payroll.domain.PayrollEvent;

/** Use case for registering a new payroll event. */
public interface RegisterPayrollEventUseCase {

  /**
   * Registers a new payroll event.
   *
   * @param event the event to register
   * @return the saved event
   */
  PayrollEvent execute(PayrollEvent event);
}
