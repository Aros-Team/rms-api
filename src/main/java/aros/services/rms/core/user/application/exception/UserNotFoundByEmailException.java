/* (C) 2026 */

package aros.services.rms.core.user.application.exception;

/** Exception thrown when a user is not found by their email address. */
public class UserNotFoundByEmailException extends RuntimeException {
  /** Creates a new exception with default message. */
  public UserNotFoundByEmailException() {
    super("No existe un usuario con este correo electrónico");
  }

  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public UserNotFoundByEmailException(String message) {
    super(message);
  }
}
