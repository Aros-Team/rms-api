/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.input;

import aros.services.rms.core.payroll.domain.PayrollEvent;
import java.time.LocalDate;
import java.util.List;

/** Use case for listing payroll events by user and date range. */
public interface ListPayrollEventsUseCase {

  /**
   * Lists payroll events for a user within a date range.
   *
   * @param userId the user id
   * @param startDate start of the period (inclusive)
   * @param endDate end of the period (inclusive)
   * @return list of matching events
   */
  List<PayrollEvent> execute(Long userId, LocalDate startDate, LocalDate endDate);
}
