/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.input;

/** Use case for deleting a payroll event. */
public interface DeletePayrollEventUseCase {

  /**
   * Deletes a payroll event by id.
   *
   * @param eventId the event id to delete
   */
  void execute(Long eventId);
}
