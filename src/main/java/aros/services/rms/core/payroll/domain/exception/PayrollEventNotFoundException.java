/* (C) 2026 */

package aros.services.rms.core.payroll.domain.exception;

/** Exception thrown when a payroll event is not found. */
public class PayrollEventNotFoundException extends RuntimeException {

  /**
   * Creates the exception with the given event id.
   *
   * @param id the event id that was not found
   */
  public PayrollEventNotFoundException(Long id) {
    super("Payroll event not found with id: " + id);
  }
}
