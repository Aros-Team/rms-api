/* (C) 2026 */

package aros.services.rms.core.purchase.application.exception;

/** Thrown when attempting to create a purchase order for an inactive supplier. */
public class SupplierInactiveException extends RuntimeException {

  /**
   * Creates exception by supplier ID.
   *
   * @param id supplier ID
   */
  public SupplierInactiveException(Long id) {
    super("Supplier is inactive: id=" + id);
  }
}
