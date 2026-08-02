/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.input;

import aros.services.rms.core.payroll.domain.Payroll;
import java.util.Optional;

/** Use case for querying payroll records. */
public interface GetPayrollUseCase {

  /**
   * Finds a payroll by user and period.
   *
   * @param userId the user id
   * @param year the year
   * @param month the month
   * @return the payroll if found
   */
  Optional<Payroll> findByUserAndPeriod(Long userId, int year, int month);

  /**
   * Finds a payroll by id.
   *
   * @param id the payroll id
   * @return the payroll if found
   */
  Optional<Payroll> findById(Long id);
}
