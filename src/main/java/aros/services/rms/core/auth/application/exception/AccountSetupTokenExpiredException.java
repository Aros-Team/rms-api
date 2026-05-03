/* (C) 2026 */

package aros.services.rms.core.auth.application.exception;

/** Exception thrown when an account setup token has expired. */
public class AccountSetupTokenExpiredException extends RuntimeException {
  /** Creates a new exception with default message. */
  public AccountSetupTokenExpiredException() {
    super("Token de configuración expirado");
  }

  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public AccountSetupTokenExpiredException(String message) {
    super(message);
  }
}
