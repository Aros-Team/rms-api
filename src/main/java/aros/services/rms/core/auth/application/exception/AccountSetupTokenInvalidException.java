/* (C) 2026 */

package aros.services.rms.core.auth.application.exception;

/** Exception thrown when an account setup token is invalid. */
public class AccountSetupTokenInvalidException extends RuntimeException {
  /** Creates a new exception with default message. */
  public AccountSetupTokenInvalidException() {
    super("Token de configuración no válido");
  }

  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public AccountSetupTokenInvalidException(String message) {
    super(message);
  }
}
