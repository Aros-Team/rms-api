/* (C) 2026 */

package aros.services.rms.core.purchase.application.exception;

/** Thrown when a supplier is not found by the given identifier. */
public class SupplierNotFoundException extends RuntimeException {

  /**
   * Creates exception by ID.
   *
   * @param id supplier ID
   */
  public SupplierNotFoundException(Long id) {
    super("Supplier not found: id=" + id);
  }
}
