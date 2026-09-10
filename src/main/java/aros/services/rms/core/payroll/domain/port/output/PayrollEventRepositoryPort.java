/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.output;

import aros.services.rms.core.payroll.domain.PayrollEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Output port for payroll event persistence operations. */
public interface PayrollEventRepositoryPort {

  /**
   * Saves a payroll event.
   *
   * @param event the event to save
   * @return the saved event
   */
  PayrollEvent save(PayrollEvent event);

  /**
   * Finds events for a user within a date range.
   *
   * @param userId the user id
   * @param startDate start of the period (inclusive)
   * @param endDate end of the period (inclusive)
   * @return list of matching events
   */
  List<PayrollEvent> findByUserAndPeriod(Long userId, LocalDate startDate, LocalDate endDate);

  /**
   * Finds a payroll event by id.
   *
   * @param id the event id
   * @return the event if found
   */
  Optional<PayrollEvent> findById(Long id);

  /**
   * Deletes a payroll event by id.
   *
   * @param id the event id
   */
  void deleteById(Long id);

  /**
   * Finds all events associated with a payroll record.
   *
   * @param payrollId the payroll id
   * @return list of matching events
   */
  List<PayrollEvent> findByPayrollId(Long payrollId);
}
