/* (C) 2026 */

package aros.services.rms.core.inventory.application.exception;

/** Thrown when a supply category with the same name already exists. */
public class SupplyCategoryAlreadyExistsException extends RuntimeException {

  /**
   * Creates exception by name.
   *
   * @param name supply category name
   */
  public SupplyCategoryAlreadyExistsException(String name) {
    super("Supply category already exists: name=" + name);
  }
}
