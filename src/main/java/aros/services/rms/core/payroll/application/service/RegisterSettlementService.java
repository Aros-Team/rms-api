/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.exception.PayrollSettlementException;
import aros.services.rms.core.payroll.domain.port.input.RegisterSettlementUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollSettlementRepositoryPort;
import lombok.RequiredArgsConstructor;

/** Service implementation for registering a new payroll settlement. */
@RequiredArgsConstructor
public class RegisterSettlementService implements RegisterSettlementUseCase {

  private final PayrollSettlementRepositoryPort settlementRepository;
  private final Logger logger;

  @Override
  public PayrollSettlement execute(PayrollSettlement settlement) {
    logger.info(
        "Registering settlement for payroll {} user {}",
        settlement.payrollId(),
        settlement.userId());

    if (settlement.amount() == null || settlement.amount().signum() <= 0) {
      throw new PayrollSettlementException("Settlement amount must be positive");
    }

    PayrollSettlement saved = settlementRepository.save(settlement);
    logger.info("Registered settlement {} for payroll {}", saved.id(), saved.payrollId());
    return saved;
  }
}
