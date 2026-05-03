/* (C) 2026 */

package aros.services.rms.core.auth.application.exception;

/** Exception thrown when an account setup token has already been used. */
public class AccountSetupTokenAlreadyUsedException extends RuntimeException {
  /** Creates a new exception with default message. */
  public AccountSetupTokenAlreadyUsedException() {
    super("Token de configuración ya ha sido utilizado");
  }

  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public AccountSetupTokenAlreadyUsedException(String message) {
    super(message);
  }
}
