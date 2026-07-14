/* (C) 2026 */

package aros.services.rms.core.product.port.input;

import aros.services.rms.core.product.domain.ProductCost;

/** Input port for calculating the production cost of a product on-the-fly. */
public interface CalculateProductCostUseCase {

  /**
   * Calculates the cost of producing a product.
   *
   * @param productId the product ID
   * @return the calculated cost with breakdown
   * @throws aros.services.rms.core.product.application.exception.ProductNotFoundException if
   *     product not found
   */
  ProductCost calculateCost(Long productId);
}
