/* (C) 2026 */
package aros.services.rms.core.purchase.application.exception;

/** Thrown when a purchase order is not found by the given identifier. */
public class PurchaseOrderNotFoundException extends RuntimeException {

  public PurchaseOrderNotFoundException(Long id) {
    super("Purchase order not found: id=" + id);
  }
}
