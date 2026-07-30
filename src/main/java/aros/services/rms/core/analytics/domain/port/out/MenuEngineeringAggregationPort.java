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

  /**
   * Loads the historical average cost of options chosen per order, grouped by product, over the
   * given period (inclusive start, exclusive end). The returned map is keyed by product_id.
   *
   * <p>For each order in the period, this sums the {@code option_recipes ×
   * supply_variants.unit_cost} of every option chosen in that order's order_details, then averages
   * those per-order option costs across all orders for each product. Products without any options
   * chosen (or without sales in the period) are absent from the map — callers should treat missing
   * entries as zero.
   *
   * <p>Used by {@code RefreshMenuEngineeringService} to compute {@code avgOptionCost} (and then
   * {@code effectiveCost = recipeCost + avgOptionCost}) for the menu engineering BCG report.
   *
   * @param start inclusive period start
   * @param end exclusive period end
   * @return map productId → average option cost, possibly empty
   */
  Map<Long, Money> loadAvgOptionCostByProduct(LocalDate start, LocalDate end);
}
