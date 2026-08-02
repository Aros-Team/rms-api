/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.input;

import aros.services.rms.core.payroll.domain.Payroll;
import java.util.List;

/** Use case for listing payroll records. */
public interface ListPayrollsUseCase {

  /**
   * Returns all payroll records.
   *
   * @return list of all payrolls
   */
  List<Payroll> findAll();

  /**
   * Returns payroll records for a given period.
   *
   * @param year the year
   * @param month the month
   * @return list of matching payrolls
   */
  List<Payroll> findByPeriod(int year, int month);

  /**
   * Returns payroll records for a given user.
   *
   * @param userId the user id
   * @return list of matching payrolls
   */
  List<Payroll> findByUserId(Long userId);
}
