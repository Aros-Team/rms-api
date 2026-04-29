/* (C) 2026 */

package aros.services.rms.core.inventory.application.exception;

import aros.services.rms.core.inventory.domain.MovementType;

/** Exception thrown when stock is insufficient for a deduction operation. */
public class InsufficientStockException extends RuntimeException {

  /**
   * Creates a new exception for insufficient stock.
   *
   * @param variantId the supply variant identifier
   * @param required the required quantity
   * @param available the available quantity
   */
  public InsufficientStockException(
      Long variantId, java.math.BigDecimal required, java.math.BigDecimal available) {
    super(
        String.format(
            "Insufficient stock for variant %d: required=%s, available=%s",
            variantId, required, available));
  }

  /**
   * Creates a new exception for insufficient stock with custom message.
   *
   * @param productId the product identifier
   * @param movementType the type of movement attempted
   * @param message the custom error message
   */
  public InsufficientStockException(Long productId, MovementType movementType, String message) {
    super(message);
  }
}
