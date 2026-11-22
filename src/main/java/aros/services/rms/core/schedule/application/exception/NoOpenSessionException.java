/* (C) 2026 */

package aros.services.rms.core.schedule.application.exception;

/** Thrown when a worker tries to clock out without an open session. */
public class NoOpenSessionException extends RuntimeException {
  /**
   * Creates a new NoOpenSessionException.
   *
   * @param message the detail message
   */
  public NoOpenSessionException(String message) {
    super(message);
  }
}
