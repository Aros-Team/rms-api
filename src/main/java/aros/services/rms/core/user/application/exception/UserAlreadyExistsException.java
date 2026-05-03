/* (C) 2026 */

package aros.services.rms.core.user.application.exception;

/** Exception thrown when attempting to create a user that already exists. */
public class UserAlreadyExistsException extends Exception {
  /** Creates a new exception with no message. */
  public UserAlreadyExistsException() {
    super();
  }

  /**
   * Creates a new exception with custom message.
   *
   * @param message the custom error message
   */
  public UserAlreadyExistsException(String message) {
    super(message);
  }
}
