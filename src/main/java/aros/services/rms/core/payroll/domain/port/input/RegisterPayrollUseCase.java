/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.input;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.Payroll;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Use case for registering a new payroll record. */
public interface RegisterPayrollUseCase {

  /**
   * Registers a new payroll for a user in a given period.
   *
   * @param command the registration command
   * @return the created payroll
   */
  Payroll register(RegisterPayrollCommand command);

  /** Command object for payroll registration. */
  record RegisterPayrollCommand(
      Long userId,
      int year,
      int month,
      LocalDate periodStart,
      LocalDate periodEnd,
      Money baseSalary,
      Money bonuses,
      Money deductions,
      BigDecimal hoursWorked,
      String notes,
      Long performedBy) {}
}
