/* (C) 2026 */

package aros.services.rms.core.auth.application.exception;

/** Exception thrown when provided credentials are invalid. */
public class InvalidCredentialsException extends RuntimeException {
  /** Creates a new exception with default message. */
  public InvalidCredentialsException() {
    super("Credenciales inválidas. Verifique su correo electrónico y contraseña");
  }

  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public InvalidCredentialsException(String message) {
    super(message);
  }
}
