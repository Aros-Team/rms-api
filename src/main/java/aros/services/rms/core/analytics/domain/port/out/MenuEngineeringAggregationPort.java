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
   * <p>For each order-detail line in the period, this sums the selection-mode contribution of every
   * option chosen in that line, then averages those per-line contributions across all order lines
   * for each product. The contribution per selected option follows the category's selection mode
   * (Phase D):
   *
   * <ul>
   *   <li>{@code SINGLE_CHOICE} with a {@code replace_supply_category_id} (substitution slot)
   *       contributes {@code optionCost − defaultSlotCost}, where {@code defaultSlotCost} is the
   *       base-recipe material cost of the product for the replaced supply category.
   *   <li>{@code REMOVE} contributes {@code −optionCost}.
   *   <li>{@code EXTRA}, {@code MULTI_SELECT} and non-replacement {@code SINGLE_CHOICE} contribute
   *       {@code +optionCost}.
   * </ul>
   *
   * <p>Order lines without any selected option contribute zero to the average. Products without any
   * sales in the period are absent from the map — callers should treat missing entries as zero.
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
