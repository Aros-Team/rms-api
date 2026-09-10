/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.input;

import aros.services.rms.core.payroll.domain.PayrollSettlement;

/** Use case for registering a new payroll settlement. */
public interface RegisterSettlementUseCase {

  /**
   * Registers a new payroll settlement.
   *
   * @param settlement the settlement to register
   * @return the saved settlement
   */
  PayrollSettlement execute(PayrollSettlement settlement);
}
