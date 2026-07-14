package aros.services.rms.core.specialselection.application.exception;

/** Exception raised when a special selection history entry is invalid. */
public class InvalidSpecialSelectionHistoryException extends RuntimeException {
  /**
   * Creates a new invalid special selection history exception.
   *
   * @param message the error message
   */
  public InvalidSpecialSelectionHistoryException(String message) {
    super(message);
  }
}
