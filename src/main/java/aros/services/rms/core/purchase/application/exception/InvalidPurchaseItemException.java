/* (C) 2026 */

package aros.services.rms.core.purchase.application.exception;

/** Thrown when quantity_received exceeds quantity_ordered for a purchase item. */
public class InvalidPurchaseItemException extends RuntimeException {

  /**
   * Creates exception by supply variant ID.
   *
   * @param supplyVariantId the supply variant ID
   */
  public InvalidPurchaseItemException(Long supplyVariantId) {
    super(
        "Invalid purchase item: quantityReceived > quantityOrdered for supplyVariantId="
            + supplyVariantId);
  }
}
