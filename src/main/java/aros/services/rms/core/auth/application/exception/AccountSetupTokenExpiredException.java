/* (C) 2026 */
package aros.services.rms.core.auth.application.exception;

public class AccountSetupTokenExpiredException extends RuntimeException {
  public AccountSetupTokenExpiredException() {
    super("Token de configuración expirado");
  }

  public AccountSetupTokenExpiredException(String message) {
    super(message);
  }
}
