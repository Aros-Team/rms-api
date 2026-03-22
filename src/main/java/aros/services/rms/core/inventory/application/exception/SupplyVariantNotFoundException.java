/* (C) 2026 */
package aros.services.rms.core.inventory.application.exception;

/** Exception thrown when a supply variant is not found. */
public class SupplyVariantNotFoundException extends RuntimeException {

  public SupplyVariantNotFoundException(Long id) {
    super("Supply variant not found: " + id);
  }
}
