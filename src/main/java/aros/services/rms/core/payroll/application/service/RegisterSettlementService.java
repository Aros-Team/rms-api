/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.exception.PayrollImmutableException;
import aros.services.rms.core.payroll.domain.exception.PayrollNotFoundException;
import aros.services.rms.core.payroll.domain.exception.PayrollSettlementException;
import aros.services.rms.core.payroll.domain.port.input.RegisterSettlementUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.PayrollSettlementRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Service implementation for registering a new payroll settlement.
 *
 * <p>The settlement must target an existing mutable payroll, belong to the same worker and fall
 * inside the payroll period. Overlapping settlement periods are rejected, which also makes a
 * repeated identical payment a conflict instead of a double count. The payroll paid amount is then
 * recomputed from all persisted settlements.
 */
@RequiredArgsConstructor
public class RegisterSettlementService implements RegisterSettlementUseCase {

  private final PayrollSettlementRepositoryPort settlementRepository;
  private final PayrollRepositoryPort payrollRepository;
  private final PayrollAggregationService aggregationService;
  private final Logger logger;

  @Override
  public PayrollSettlement execute(PayrollSettlement settlement) {
    logger.info(
        "Registering settlement for payroll {} user {}",
        settlement.payrollId(),
        settlement.userId());

    if (settlement.amount() == null || !settlement.amount().isPositive()) {
      throw new PayrollSettlementException("El monto de la liquidación debe ser positivo");
    }
    if (settlement.payrollId() == null) {
      throw new PayrollSettlementException("El payrollId es obligatorio");
    }

    Payroll payroll =
        payrollRepository
            .findById(settlement.payrollId())
            .orElseThrow(() -> new PayrollNotFoundException(settlement.payrollId()));

    if (payroll.isImmutable()) {
      throw new PayrollImmutableException(payroll.id(), payroll.status().name());
    }
    if (!payroll.userId().equals(settlement.userId())) {
      throw new PayrollSettlementException(
          "El usuario de la liquidación "
              + settlement.userId()
              + " no coincide con el usuario de la nómina "
              + payroll.userId());
    }
    if (!settlement.fallsWithin(payroll)) {
      throw new PayrollSettlementException(
          "El periodo de la liquidación "
              + settlement.periodStart()
              + ".."
              + settlement.periodEnd()
              + " está fuera del periodo de nómina "
              + payroll.periodStart()
              + ".."
              + payroll.periodEnd());
    }

    List<PayrollSettlement> existing = settlementRepository.findByPayrollId(payroll.id());
    Money alreadyPaid = Money.zero(settlement.amount().currency());
    for (PayrollSettlement other : existing) {
      if (settlement.overlaps(other)) {
        throw new PayrollSettlementException(
            "El periodo de la liquidación "
                + settlement.periodStart()
                + ".."
                + settlement.periodEnd()
                + " se superpone con la liquidación "
                + other.id()
                + " ("
                + other.periodStart()
                + ".."
                + other.periodEnd()
                + ")");
      }
      alreadyPaid = alreadyPaid.plus(other.amount());
    }

    Money pending = payroll.netAmount().minus(alreadyPaid);
    if (settlement.amount().isGreaterThan(pending)) {
      throw new PayrollSettlementException(
          "El monto de la liquidación "
              + settlement.amount()
              + " supera el monto pendiente "
              + pending
              + " de la nómina "
              + payroll.id());
    }

    PayrollSettlement saved = settlementRepository.save(settlement);
    aggregationService.recompute(payroll);
    logger.info("Registered settlement {} for payroll {}", saved.id(), saved.payrollId());
    return saved;
  }
}
