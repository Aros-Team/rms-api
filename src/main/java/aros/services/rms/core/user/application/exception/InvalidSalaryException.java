/* (C) 2026 */

package aros.services.rms.core.user.application.exception;

/** Exception thrown when an invalid salary value is provided. */
public class InvalidSalaryException extends RuntimeException {

  /**
   * Creates an InvalidSalaryException.
   *
   * @param message the detail message
   */
  public InvalidSalaryException(String message) {
    super(message);
  }
}
