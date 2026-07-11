package aros.services.rms.core.schedule.application.exception;

/** Thrown when a requested schedule is not found. */
public class ScheduleNotFoundException extends RuntimeException {
  /**
   * Creates a new ScheduleNotFoundException.
   *
   * @param message the detail message
   */
  public ScheduleNotFoundException(String message) {
    super(message);
  }
}
