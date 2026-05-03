/* (C) 2026 */

package aros.services.rms.core.area.application.exception;

/** Exception thrown when attempting to create an area with a name that already exists. */
public class AreaAlreadyExistsException extends RuntimeException {

  /**
   * Creates a new exception for duplicate area name.
   *
   * @param name the duplicate area name
   */
  public AreaAlreadyExistsException(String name) {
    super("Area already exists: " + name);
  }
}
