/* (C) 2026 */

package aros.services.rms.core.product.port.input;

import aros.services.rms.core.product.domain.ProductCost;
import java.time.YearMonth;

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

  /**
   * Calculates the cost of producing a product for a given period.
   *
   * @param productId the product ID
   * @param period the year-month period for labor cost calculation (null defaults to current month)
   * @return the calculated cost with breakdown
   * @throws aros.services.rms.core.product.application.exception.ProductNotFoundException if
   *     product not found
   */
  ProductCost calculateCost(Long productId, YearMonth period);
}
