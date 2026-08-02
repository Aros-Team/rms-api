/* (C) 2026 */

package aros.services.rms.core.payroll.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollStatus;
import aros.services.rms.core.payroll.domain.exception.InvalidPayrollPeriodException;
import aros.services.rms.core.payroll.domain.exception.PayrollImmutableException;
import aros.services.rms.core.payroll.domain.exception.PayrollNotFoundException;
import aros.services.rms.core.payroll.domain.port.input.UpdatePayrollUseCase;
import aros.services.rms.core.payroll.domain.port.output.PayrollRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;

/** Service implementation for updating an existing payroll record. */
@RequiredArgsConstructor
public class UpdatePayrollService implements UpdatePayrollUseCase {

  private final PayrollRepositoryPort payrollRepositoryPort;
  private final Logger logger;

  @Override
  public Payroll update(Long id, UpdatePayrollCommand cmd) {
    Payroll existing =
        payrollRepositoryPort.findById(id).orElseThrow(() -> new PayrollNotFoundException(id));

    // Only PENDING records can be modified
    if (!existing.isPending()) {
      throw new PayrollImmutableException(id, existing.status().name());
    }

    // If status is changing, validate the transition
    if (cmd.status() != null && cmd.status() != existing.status()) {
      if (!existing.status().canTransitionTo(cmd.status())) {
        throw new PayrollImmutableException(id, existing.status().name());
      }
    }

    // Use provided values or keep existing
    Money baseSalary = cmd.baseSalary() != null ? cmd.baseSalary() : existing.baseSalary();
    Money bonuses = cmd.bonuses() != null ? cmd.bonuses() : existing.bonuses();
    Money deductions = cmd.deductions() != null ? cmd.deductions() : existing.deductions();
    BigDecimal hoursWorked = cmd.hoursWorked() != null ? cmd.hoursWorked() : existing.hoursWorked();
    PayrollStatus status = cmd.status() != null ? cmd.status() : existing.status();
    String notes = cmd.notes() != null ? cmd.notes() : existing.notes();

    // Recalculate netAmount
    Money netAmount = baseSalary.plus(bonuses).minus(deductions);
    if (netAmount.isNegative()) {
      throw new InvalidPayrollPeriodException("netAmount must not be negative");
    }

    Payroll updated =
        new Payroll(
            existing.id(),
            existing.userId(),
            existing.period(),
            existing.periodStart(),
            existing.periodEnd(),
            baseSalary,
            bonuses,
            deductions,
            netAmount,
            hoursWorked,
            status,
            notes,
            existing.registeredBy(),
            existing.createdAt(),
            Instant.now());

    Payroll saved = payrollRepositoryPort.save(updated);
    logger.info("Updated payroll {} to status {}, netAmount={}", id, status, netAmount);
    return saved;
  }
}
