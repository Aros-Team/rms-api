/* (C) 2026 */

package aros.services.rms.core.floorlayout.domain.exception;

/** Raised when a submitted floor layout violates the grid or overlap rules. */
public class InvalidFloorLayoutException extends RuntimeException {

  /**
   * Creates the exception with a user-facing explanation.
   *
   * @param message explanation of the violated rule
   */
  public InvalidFloorLayoutException(String message) {
    super(message);
  }
}
