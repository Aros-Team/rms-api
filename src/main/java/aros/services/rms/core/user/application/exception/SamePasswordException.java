/* (C) 2026 */

package aros.services.rms.core.user.application.exception;

/** Exception thrown when the new password is the same as the current password. */
public class SamePasswordException extends RuntimeException {
  /** Creates a new exception with default message. */
  public SamePasswordException() {
    super("La nueva contraseña no puede ser igual a la anterior");
  }

  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public SamePasswordException(String message) {
    super(message);
  }
}
