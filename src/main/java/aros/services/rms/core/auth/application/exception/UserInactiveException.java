/* (C) 2026 */

package aros.services.rms.core.auth.application.exception;

/** Exception thrown when an inactive user attempts to authenticate. */
public class UserInactiveException extends RuntimeException {
  /** Creates exception with default message. */
  public UserInactiveException() {
    super("Usuario inactivo. Contacte al administrador para activar su cuenta");
  }

  /**
   * Creates exception with custom message.
   *
   * @param message error message
   */
  public UserInactiveException(String message) {
    super(message);
  }
}
