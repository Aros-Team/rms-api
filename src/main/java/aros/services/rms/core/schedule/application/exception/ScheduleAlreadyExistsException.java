package aros.services.rms.core.schedule.application.exception;

/** Thrown when attempting to create a schedule that already exists. */
public class ScheduleAlreadyExistsException extends RuntimeException {
  /**
   * Creates a new ScheduleAlreadyExistsException.
   *
   * @param message the detail message
   */
  public ScheduleAlreadyExistsException(String message) {
    super(message);
  }
}
