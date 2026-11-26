/* (C) 2026 */

package aros.services.rms.core.area.application.exception;

/** Raised when making an area exclusive would break an existing multi-area assignment. */
public class ExclusiveAreaConflictException extends IllegalArgumentException {

  /**
   * Creates the exception.
   *
   * @param message human-readable explanation
   */
  public ExclusiveAreaConflictException(String message) {
    super(message);
  }
}
