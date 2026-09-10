/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for PayrollSettlementEntity persistence. */
@Repository
public interface PayrollSettlementRepository extends JpaRepository<PayrollSettlementEntity, Long> {

  /**
   * Finds settlements for a user within a date range.
   *
   * @param userId the user id
   * @param startDate start of the period (inclusive)
   * @param endDate end of the period (inclusive)
   * @return list of matching settlements
   */
  List<PayrollSettlementEntity> findByUserIdAndPeriodStartBetween(
      Long userId, LocalDate startDate, LocalDate endDate);

  /**
   * Finds all settlements for a given payroll record.
   *
   * @param payrollId the payroll id
   * @return list of matching settlements
   */
  List<PayrollSettlementEntity> findByPayrollId(Long payrollId);
}
