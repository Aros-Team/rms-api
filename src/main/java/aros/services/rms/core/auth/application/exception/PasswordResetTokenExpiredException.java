/* (C) 2026 */

package aros.services.rms.core.auth.application.exception;

/** Exception thrown when a password reset token has expired. */
public class PasswordResetTokenExpiredException extends RuntimeException {
  /** Creates a new exception with default message. */
  public PasswordResetTokenExpiredException() {
    super("El token de recuperación ha expirado");
  }

  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public PasswordResetTokenExpiredException(String message) {
    super(message);
  }
}
