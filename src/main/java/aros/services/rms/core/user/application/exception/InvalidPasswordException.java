/* (C) 2026 */

package aros.services.rms.core.user.application.exception;

/** Exception thrown when the current password is invalid during password change. */
public class InvalidPasswordException extends RuntimeException {
  /** Creates a new exception with default message. */
  public InvalidPasswordException() {
    super("La contraseña actual no es correcta");
  }

  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public InvalidPasswordException(String message) {
    super(message);
  }
}
