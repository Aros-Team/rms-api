/* (C) 2026 */
package aros.services.rms.core.auth.application.exception;

public class AccountSetupTokenInvalidException extends RuntimeException {
  public AccountSetupTokenInvalidException() {
    super("Token de configuración no válido");
  }

  public AccountSetupTokenInvalidException(String message) {
    super(message);
  }
}
