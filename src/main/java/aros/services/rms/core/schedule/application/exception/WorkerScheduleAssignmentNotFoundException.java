package aros.services.rms.core.schedule.application.exception;

/** Thrown when a worker schedule assignment is not found. */
public class WorkerScheduleAssignmentNotFoundException extends RuntimeException {
  /**
   * Creates a new WorkerScheduleAssignmentNotFoundException.
   *
   * @param message the detail message
   */
  public WorkerScheduleAssignmentNotFoundException(String message) {
    super(message);
  }
}
