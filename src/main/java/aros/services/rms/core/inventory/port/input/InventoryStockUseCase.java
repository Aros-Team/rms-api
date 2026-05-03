/* (C) 2026 */

package aros.services.rms.core.inventory.port.input;

import java.util.List;

/** Input port for inventory stock availability checks. */
public interface InventoryStockUseCase {

  /**
   * Checks if a product with selected options is available (has sufficient stock in Cocina).
   * Products without a recipe are considered available.
   *
   * @param productId the product id
   * @param selectedOptionIds list of selected option ids (can be null or empty)
   * @return true if available, false otherwise
   */
  boolean isAvailable(Long productId, List<Long> selectedOptionIds);
}
