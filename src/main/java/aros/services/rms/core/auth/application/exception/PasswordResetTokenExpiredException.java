/* (C) 2026 */
package aros.services.rms.core.auth.application.exception;

public class PasswordResetTokenExpiredException extends RuntimeException {
  public PasswordResetTokenExpiredException() {
    super("El token de recuperación ha expirado");
  }

  public PasswordResetTokenExpiredException(String message) {
    super(message);
  }
}
