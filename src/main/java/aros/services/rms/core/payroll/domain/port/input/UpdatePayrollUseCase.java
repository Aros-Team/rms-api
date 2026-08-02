/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.input;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollStatus;
import java.math.BigDecimal;

/** Use case for updating an existing payroll record. */
public interface UpdatePayrollUseCase {

  /**
   * Updates a payroll record.
   *
   * @param id the payroll id
   * @param command the update command
   * @return the updated payroll
   */
  Payroll update(Long id, UpdatePayrollCommand command);

  /** Command object for payroll update. */
  record UpdatePayrollCommand(
      Money baseSalary,
      Money bonuses,
      Money deductions,
      BigDecimal hoursWorked,
      PayrollStatus status,
      String notes,
      Long performedBy) {}
}
