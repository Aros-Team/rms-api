/* (C) 2026 */

package aros.services.rms.core.payroll.domain.exception;

/** Thrown when a payroll period is invalid (e.g. start after end, wrong month range). */
public class InvalidPayrollPeriodException extends RuntimeException {

  /**
   * Creates the exception.
   *
   * @param message the error message
   */
  public InvalidPayrollPeriodException(String message) {
    super(message);
  }
}
