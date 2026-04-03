/* (C) 2026 */
package aros.services.rms.core.inventory.application.exception;

/** Thrown when a supply with the same name already exists. */
public class SupplyAlreadyExistsException extends RuntimeException {

  public SupplyAlreadyExistsException(String name) {
    super("Supply already exists: name=" + name);
  }
}
