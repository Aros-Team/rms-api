/* (C) 2026 */
package aros.services.rms.core.auth.application.exception;

public class InvalidCredentialsException extends RuntimeException {
  public InvalidCredentialsException() {
    super("Credenciales inválidas. Verifique su correo electrónico y contraseña");
  }

  public InvalidCredentialsException(String message) {
    super(message);
  }
}