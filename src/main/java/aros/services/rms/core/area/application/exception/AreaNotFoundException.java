/* (C) 2026 */

package aros.services.rms.core.area.application.exception;

/** Exception thrown when an area is not found by its identifier. */
public class AreaNotFoundException extends RuntimeException {

  /**
   * Creates a new exception for missing area.
   *
   * @param id the area identifier that was not found
   */
  public AreaNotFoundException(Long id) {
    super("Area not found: " + id);
  }

  /**
   * Creates a new exception for missing area.
   *
   * @param msg custom message of error
   */
  public AreaNotFoundException(String msg) {
    super(msg);
  }
}
