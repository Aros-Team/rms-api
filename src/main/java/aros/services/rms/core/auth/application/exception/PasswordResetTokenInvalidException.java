/* (C) 2026 */
package aros.services.rms.core.auth.application.exception;

public class PasswordResetTokenInvalidException extends RuntimeException {
  public PasswordResetTokenInvalidException() {
    super("Token de recuperación inválido");
  }

  public PasswordResetTokenInvalidException(String message) {
    super(message);
  }
}
