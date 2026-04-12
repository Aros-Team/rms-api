/* (C) 2026 */
package aros.services.rms.core.user.application.exception;

public class SamePasswordException extends RuntimeException {
  public SamePasswordException() {
    super("La nueva contraseña no puede ser igual a la anterior");
  }

  public SamePasswordException(String message) {
    super(message);
  }
}