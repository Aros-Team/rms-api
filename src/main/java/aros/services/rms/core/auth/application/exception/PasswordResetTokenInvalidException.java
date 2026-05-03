/* (C) 2026 */

package aros.services.rms.core.auth.application.exception;

/** Exception thrown when a password reset token is invalid. */
public class PasswordResetTokenInvalidException extends RuntimeException {
  /** Creates a new exception with default message. */
  public PasswordResetTokenInvalidException() {
    super("Token de recuperación inválido");
  }

  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public PasswordResetTokenInvalidException(String message) {
    super(message);
  }
}
