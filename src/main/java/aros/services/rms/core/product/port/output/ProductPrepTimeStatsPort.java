/* (C) 2026 */

package aros.services.rms.core.product.port.output;

import aros.services.rms.core.product.domain.ProductPrepTimeStats;

/** Output port for measured product preparation-time statistics. */
public interface ProductPrepTimeStatsPort {

  /**
   * Loads all valid measured samples for a product.
   *
   * @param productId the product identifier
   * @return measured statistics, never null
   */
  ProductPrepTimeStats findByProductId(Long productId);
}
