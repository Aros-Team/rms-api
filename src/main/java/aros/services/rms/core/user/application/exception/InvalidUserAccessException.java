/* (C) 2026 */

package aros.services.rms.core.user.application.exception;

/** Thrown when a role, status or area change is not allowed. */
public class InvalidUserAccessException extends IllegalArgumentException {

  /**
   * Creates the exception.
   *
   * @param message the human-readable detail
   */
  public InvalidUserAccessException(String message) {
    super(message);
  }
}
