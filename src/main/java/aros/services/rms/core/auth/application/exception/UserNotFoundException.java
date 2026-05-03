/* (C) 2026 */

package aros.services.rms.core.auth.application.exception;

/** Exception thrown when a user is not found during authentication operations. */
public class UserNotFoundException extends Exception {
  /**
   * Creates a user not found exception.
   *
   * @param message error message
   */
  public UserNotFoundException(String message) {
    super(message);
  }
}
