/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.input;

import aros.services.rms.core.payroll.domain.PayrollSettlement;
import java.time.LocalDate;
import java.util.List;

/** Use case for listing settlements by user and date range. */
public interface ListSettlementsUseCase {

  /**
   * Lists settlements for a user within a date range.
   *
   * @param userId the user id
   * @param startDate start of the period (inclusive)
   * @param endDate end of the period (inclusive)
   * @return list of matching settlements
   */
  List<PayrollSettlement> execute(Long userId, LocalDate startDate, LocalDate endDate);
}
