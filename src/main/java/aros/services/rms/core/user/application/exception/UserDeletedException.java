/* (C) 2026 */

package aros.services.rms.core.user.application.exception;

/** Exception thrown when a deleted user attempts to perform an operation. */
public class UserDeletedException extends RuntimeException {
  /** Creates a new exception with default message. */
  public UserDeletedException() {
    super("Tu cuenta ha sido eliminada del sistema");
  }

  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public UserDeletedException(String message) {
    super(message);
  }
}
