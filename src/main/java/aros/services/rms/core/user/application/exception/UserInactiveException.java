/* (C) 2026 */

package aros.services.rms.core.user.application.exception;

/** Exception thrown when an inactive user attempts to perform an operation. */
public class UserInactiveException extends RuntimeException {
  /** Creates a new exception with default message. */
  public UserInactiveException() {
    super("Tu cuenta está inactiva. Contacta al administrador para activarla");
  }

  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public UserInactiveException(String message) {
    super(message);
  }
}
