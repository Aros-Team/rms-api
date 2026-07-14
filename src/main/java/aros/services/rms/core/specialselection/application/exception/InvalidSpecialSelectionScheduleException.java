package aros.services.rms.core.specialselection.application.exception;

/** Exception raised when a special selection schedule is invalid. */
public class InvalidSpecialSelectionScheduleException extends RuntimeException {
  /**
   * Creates a new invalid special selection schedule exception.
   *
   * @param message the error message
   */
  public InvalidSpecialSelectionScheduleException(String message) {
    super(message);
  }
}
