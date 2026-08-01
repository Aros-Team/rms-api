/* (C) 2026 */

package aros.services.rms.core.product.port.input;

import aros.services.rms.core.product.domain.ProductCostBreakdown;

/** Input port for retrieving a product material-cost projection. */
public interface GetProductCostBreakdownUseCase {

  /**
   * Gets the base and projected option costs for a product.
   *
   * @param productId the product identifier
   * @return the product cost breakdown
   * @throws aros.services.rms.core.product.application.exception.ProductNotFoundException when the
   *     product does not exist
   */
  ProductCostBreakdown execute(Long productId);
}
