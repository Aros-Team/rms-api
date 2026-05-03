/* (C) 2026 */

package aros.services.rms.core.user.application.exception;

/** Exception thrown when a user is not found by their identifier. */
public class UserNotFoundException extends RuntimeException {
  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public UserNotFoundException(String message) {
    super(message);
  }
}
