/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.output;

import aros.services.rms.core.payroll.domain.PayrollSettlement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Output port for payroll settlement persistence operations. */
public interface PayrollSettlementRepositoryPort {

  /**
   * Saves a payroll settlement.
   *
   * @param settlement the settlement to save
   * @return the saved settlement
   */
  PayrollSettlement save(PayrollSettlement settlement);

  /**
   * Finds a settlement by id.
   *
   * @param id the settlement id
   * @return the settlement if found
   */
  Optional<PayrollSettlement> findById(Long id);

  /**
   * Deletes a settlement by id.
   *
   * @param id the settlement id
   */
  void deleteById(Long id);

  /**
   * Finds settlements for a user within a date range.
   *
   * @param userId the user id
   * @param startDate start of the period (inclusive)
   * @param endDate end of the period (inclusive)
   * @return list of matching settlements
   */
  List<PayrollSettlement> findByUserAndPeriod(Long userId, LocalDate startDate, LocalDate endDate);

  /**
   * Finds all settlements associated with a payroll record.
   *
   * @param payrollId the payroll id
   * @return list of matching settlements
   */
  List<PayrollSettlement> findByPayrollId(Long payrollId);
}
