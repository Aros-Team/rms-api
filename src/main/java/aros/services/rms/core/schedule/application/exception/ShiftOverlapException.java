package aros.services.rms.core.schedule.application.exception;

/** Thrown when a worker's shifts overlap with an existing assignment. */
public class ShiftOverlapException extends RuntimeException {
  private final String details;

  /**
   * Creates a new ShiftOverlapException with the given message as details.
   *
   * @param message the detail message
   */
  public ShiftOverlapException(String message) {
    super(message);
    this.details = message;
  }

  /**
   * Creates a new ShiftOverlapException with the given message and details.
   *
   * @param message the detail message
   * @param details additional overlap details
   */
  public ShiftOverlapException(String message, String details) {
    super(message);
    this.details = details;
  }

  /**
   * Returns the details of the overlap.
   *
   * @return overlap details
   */
  public String getDetails() {
    return details;
  }
}
