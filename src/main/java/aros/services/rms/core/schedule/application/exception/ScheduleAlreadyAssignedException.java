/* (C) 2026 */

package aros.services.rms.core.schedule.application.exception;

/** Thrown when a schedule is assigned twice to the same worker. */
public class ScheduleAlreadyAssignedException extends RuntimeException {
  /**
   * Creates a new ScheduleAlreadyAssignedException.
   *
   * @param message the detail message
   */
  public ScheduleAlreadyAssignedException(String message) {
    super(message);
  }
}
