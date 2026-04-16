/* (C) 2026 */
package aros.services.rms.core.auth.application.exception;

public class UserInactiveException extends RuntimeException {
  public UserInactiveException() {
    super("Usuario inactivo. Contacte al administrador para activar su cuenta");
  }

  public UserInactiveException(String message) {
    super(message);
  }
}
