/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.input;

/** Use case for voiding an existing payroll settlement. */
public interface VoidSettlementUseCase {

  /**
   * Voids a settlement by id and restores the payroll pending amount.
   *
   * @param settlementId the settlement id to void
   */
  void execute(Long settlementId);
}
