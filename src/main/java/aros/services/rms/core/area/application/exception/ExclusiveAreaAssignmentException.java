/* (C) 2026 */

package aros.services.rms.core.area.application.exception;

/** Raised when an exclusive area is combined with other areas for the same worker. */
public class ExclusiveAreaAssignmentException extends IllegalArgumentException {

  /**
   * Creates the exception.
   *
   * @param message human-readable explanation
   */
  public ExclusiveAreaAssignmentException(String message) {
    super(message);
  }
}
