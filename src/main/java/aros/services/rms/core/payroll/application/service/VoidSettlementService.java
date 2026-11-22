/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.exception.PayrollImmutableException;
import aros.services.rms.core.payroll.domain.exception.PayrollNotFoundException;
import aros.services.rms.core.payroll.domain.exception.PayrollSettlementNotFoundException;
import aros.services.rms.core.payroll.domain.port.input.VoidSettlementUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollSettlementRepositoryPort;
import lombok.RequiredArgsConstructor;

/**
 * Service implementation for voiding (deleting) a payroll settlement.
 *
 * <p>Voiding removes the payment row and recomputes the payroll paid amount from the remaining
 * settlements, so the pending amount is restored exactly.
 */
@RequiredArgsConstructor
public class VoidSettlementService implements VoidSettlementUseCase {

  private final PayrollSettlementRepositoryPort settlementRepository;
  private final PayrollRepositoryPort payrollRepository;
  private final PayrollAggregationService aggregationService;
  private final Logger logger;

  @Override
  public void execute(Long settlementId) {
    logger.info("Voiding payroll settlement {}", settlementId);
    PayrollSettlement settlement =
        settlementRepository
            .findById(settlementId)
            .orElseThrow(() -> new PayrollSettlementNotFoundException(settlementId));

    Payroll payroll =
        payrollRepository
            .findById(settlement.payrollId())
            .orElseThrow(() -> new PayrollNotFoundException(settlement.payrollId()));

    if (payroll.isImmutable()) {
      throw new PayrollImmutableException(payroll.id(), payroll.status().name());
    }

    settlementRepository.deleteById(settlementId);
    aggregationService.recompute(payroll);
    logger.info("Voided payroll settlement {}", settlementId);
  }
}
