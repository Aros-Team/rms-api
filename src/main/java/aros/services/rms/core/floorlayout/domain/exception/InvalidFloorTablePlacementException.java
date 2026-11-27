/* (C) 2026 */

package aros.services.rms.core.floorlayout.domain.exception;

/** Raised when a table placement is invalid or lands outside a service area. */
public class InvalidFloorTablePlacementException extends RuntimeException {

  /**
   * Creates the exception with a user-facing explanation.
   *
   * @param message explanation of the violated rule
   */
  public InvalidFloorTablePlacementException(String message) {
    super(message);
  }
}
