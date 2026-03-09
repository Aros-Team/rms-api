/* (C) 2026 */
package aros.services.rms.core.area.application.exception;

/** Exception thrown when an area is not found by its identifier. */
public class AreaNotFoundException extends RuntimeException {

  public AreaNotFoundException(Long id) {
    super("Area not found: " + id);
  }
}
