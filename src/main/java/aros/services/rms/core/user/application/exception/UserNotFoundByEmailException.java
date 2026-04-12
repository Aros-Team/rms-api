/* (C) 2026 */
package aros.services.rms.core.user.application.exception;

public class UserNotFoundByEmailException extends RuntimeException {
  public UserNotFoundByEmailException() {
    super("No existe un usuario con este correo electrónico");
  }

  public UserNotFoundByEmailException(String message) {
    super(message);
  }
}