/* (C) 2026 */

package aros.services.rms.core.schedule.application.exception;

/** Thrown when a worker tries to clock in while a session is already open. */
public class OpenSessionAlreadyExistsException extends RuntimeException {
  /**
   * Creates a new OpenSessionAlreadyExistsException.
   *
   * @param message the detail message
   */
  public OpenSessionAlreadyExistsException(String message) {
    super(message);
  }
}
