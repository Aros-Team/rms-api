/* (C) 2026 */
package aros.services.rms.core.user.application.exception;

public class UserInactiveException extends RuntimeException {
  public UserInactiveException() {
    super("Tu cuenta está inactiva. Contacta al administrador para activarla");
  }

  public UserInactiveException(String message) {
    super(message);
  }
}
