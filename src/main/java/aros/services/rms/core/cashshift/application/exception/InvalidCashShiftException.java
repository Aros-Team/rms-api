/* (C) 2026 */

package aros.services.rms.core.cashshift.application.exception;

/** Thrown when a cash-shift request is invalid. Maps to HTTP 400. */
public class InvalidCashShiftException extends RuntimeException {

  /**
   * Creates the exception.
   *
   * @param message the human-readable reason
   */
  public InvalidCashShiftException(String message) {
    super(message);
  }
}
