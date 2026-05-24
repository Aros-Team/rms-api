package aros.services.rms.core.schedule.application.exception;

/** Thrown when an operation on a schedule fails because it still has assignments. */
public class ScheduleHasAssignmentsException extends RuntimeException {
  /**
   * Creates a new ScheduleHasAssignmentsException.
   *
   * @param message the detail message
   */
  public ScheduleHasAssignmentsException(String message) {
    super(message);
  }
}
