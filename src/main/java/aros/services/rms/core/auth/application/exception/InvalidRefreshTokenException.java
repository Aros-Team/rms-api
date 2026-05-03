/* (C) 2026 */

package aros.services.rms.core.auth.application.exception;

/** Exception thrown when a refresh token is invalid or expired. */
public class InvalidRefreshTokenException extends Exception {
  /** Creates a new exception with no message. */
  public InvalidRefreshTokenException() {
    super();
  }

  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public InvalidRefreshTokenException(String message) {
    super(message);
  }
}
