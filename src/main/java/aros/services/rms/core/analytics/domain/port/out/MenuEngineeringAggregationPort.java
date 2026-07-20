/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.out;

import aros.services.rms.core.common.money.domain.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Output port for aggregating raw data (products, sales, recipes) into menu engineering inputs. */
public interface MenuEngineeringAggregationPort {

  /** Active product with category info. */
  record ActiveProduct(
      Long id, String name, Money basePrice, Long categoryId, String categoryName) {}

  /** Per-product sales aggregation. */
  record SalesData(Long productId, int unitsSold, Money revenue) {}

  /**
   * Loads all active products with their category names.
   *
   * @return list of active products
   */
  List<ActiveProduct> loadActiveProducts();

  /**
   * Aggregates order detail sales per product within a date range.
   *
   * @param start start date (inclusive)
   * @param end end date (exclusive)
   * @return list of per-product sales data
   */
  List<SalesData> loadSalesByProduct(LocalDate start, LocalDate end);

  /**
   * Computes total recipe cost per product from product_recipes × supply_variants.
   *
   * @return map of productId → total recipe cost
   */
  Map<Long, Money> loadRecipeCostByProduct();
}
