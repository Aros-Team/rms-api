/* (C) 2026 */

package aros.services.rms.core.payroll.domain.exception;

/** Thrown when attempting to modify a payroll that is in an immutable state (e.g. PAID). */
public class PayrollImmutableException extends RuntimeException {

  /**
   * Creates the exception.
   *
   * @param id the payroll id
   * @param currentStatus the current status
   */
  public PayrollImmutableException(Long id, String currentStatus) {
    super("Payroll " + id + " is " + currentStatus + " and cannot be modified");
  }
}
