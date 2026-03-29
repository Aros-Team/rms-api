/* (C) 2026 */
package aros.services.rms.core.inventory.application.exception;

import aros.services.rms.core.inventory.domain.MovementType;

/** Exception thrown when stock is insufficient for a deduction operation. */
public class InsufficientStockException extends RuntimeException {

  public InsufficientStockException(
      Long variantId, java.math.BigDecimal required, java.math.BigDecimal available) {
    super(
        String.format(
            "Insufficient stock for variant %d: required=%s, available=%s",
            variantId, required, available));
  }

  public InsufficientStockException(Long productId, MovementType movementType, String message) {
    super(message);
  }
}
