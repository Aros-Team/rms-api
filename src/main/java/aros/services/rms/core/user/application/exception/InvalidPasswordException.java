/* (C) 2026 */
package aros.services.rms.core.user.application.exception;

public class InvalidPasswordException extends RuntimeException {
  public InvalidPasswordException() {
    super("La contraseña actual no es correcta");
  }

  public InvalidPasswordException(String message) {
    super(message);
  }
}
