/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollStatus;
import aros.services.rms.core.payroll.domain.exception.PayrollImmutableException;
import aros.services.rms.core.payroll.domain.exception.PayrollNotFoundException;
import aros.services.rms.core.payroll.domain.port.input.UpdatePayrollUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;

/**
 * Service implementation for updating an existing payroll record.
 *
 * <p>Manual bonuses and deductions are the non-event component. After a partial update the payroll
 * is re-aggregated so the persisted event totals remain part of the final bonuses/deductions.
 */
@RequiredArgsConstructor
public class UpdatePayrollService implements UpdatePayrollUseCase {

  private final PayrollRepositoryPort payrollRepositoryPort;
  private final PayrollAggregationService aggregationService;
  private final Logger logger;

  @Override
  public Payroll update(Long id, UpdatePayrollCommand cmd) {
    Payroll existing =
        payrollRepositoryPort.findById(id).orElseThrow(() -> new PayrollNotFoundException(id));

    if (existing.isImmutable()) {
      throw new PayrollImmutableException(id, existing.status().name());
    }

    if (cmd.status() != null && cmd.status() != existing.status()) {
      if (!existing.status().canTransitionTo(cmd.status())) {
        throw new PayrollImmutableException(id, existing.status().name());
      }
    }

    Money baseSalary = cmd.baseSalary() != null ? cmd.baseSalary() : existing.baseSalary();
    Money manualBonuses = cmd.bonuses() != null ? cmd.bonuses() : existing.manualBonuses();
    Money manualDeductions =
        cmd.deductions() != null ? cmd.deductions() : existing.manualDeductions();
    BigDecimal hoursWorked = cmd.hoursWorked() != null ? cmd.hoursWorked() : existing.hoursWorked();
    PayrollStatus status = cmd.status() != null ? cmd.status() : existing.status();
    String notes = cmd.notes() != null ? cmd.notes() : existing.notes();

    Payroll candidate =
        existing.withManualTotals(
            baseSalary, manualBonuses, manualDeductions, hoursWorked, status, notes, Instant.now());

    Payroll saved = aggregationService.recompute(candidate);
    logger.info(
        "Updated payroll {} to status {}, netAmount={}", id, saved.status(), saved.netAmount());
    return saved;
  }
}
