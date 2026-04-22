/* (C) 2026 */
package aros.services.rms.core.auth.application.exception;

public class AccountSetupTokenAlreadyUsedException extends RuntimeException {
  public AccountSetupTokenAlreadyUsedException() {
    super("Token de configuración ya ha sido utilizado");
  }

  public AccountSetupTokenAlreadyUsedException(String message) {
    super(message);
  }
}
