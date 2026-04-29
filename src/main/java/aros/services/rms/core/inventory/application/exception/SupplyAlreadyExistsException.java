/* (C) 2026 */

package aros.services.rms.core.inventory.application.exception;

/** Thrown when a supply with the same name already exists. */
public class SupplyAlreadyExistsException extends RuntimeException {

  /**
   * Creates exception by name.
   *
   * @param name supply name
   */
  public SupplyAlreadyExistsException(String name) {
    super("Supply already exists: name=" + name);
  }
}
