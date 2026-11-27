/* (C) 2026 */

package aros.services.rms.core.product.port.input;

import aros.services.rms.core.product.domain.ProductPrepTime;

/** Input port for resolving a product's estimated and measured preparation times. */
public interface GetProductPrepTimeUseCase {

  /**
   * Resolves the preparation time used for costing.
   *
   * @param productId the product identifier
   * @return resolved prep time
   */
  ProductPrepTime get(Long productId);
}
