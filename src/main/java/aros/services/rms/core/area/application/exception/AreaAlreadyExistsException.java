/* (C) 2026 */
package aros.services.rms.core.area.application.exception;

/** Exception thrown when attempting to create an area with a name that already exists. */
public class AreaAlreadyExistsException extends RuntimeException {

  public AreaAlreadyExistsException(String name) {
    super("Area already exists: " + name);
  }
}
