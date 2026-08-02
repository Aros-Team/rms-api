/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.adapter;

import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringAggregationPort;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringAggregationPort.ActiveProduct;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringAggregationPort.SalesData;
import aros.services.rms.core.category.domain.OptionSelectionType;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** JPA-backed adapter that runs aggregation queries for menu engineering BCG computation. */
@Component
@RequiredArgsConstructor
public class MenuEngineeringAggregationJpaAdapter implements MenuEngineeringAggregationPort {

  private static final Currency COP = Currency.getInstance("COP");

  private final EntityManager entityManager;
  private final ProductOptionRepositoryPort productOptionRepositoryPort;

  @Override
  public List<ActiveProduct> loadActiveProducts() {
    String sql =
        """
        SELECT p.id, p.name, p.base_price, p.category_id, COALESCE(c.name, '') as category_name
        FROM products p
        LEFT JOIN categories c ON p.category_id = c.id
        WHERE p.active = TRUE
        """;

    Query query = entityManager.createNativeQuery(sql);
    @SuppressWarnings("unchecked")
    List<Object[]> rows = query.getResultList();

    List<ActiveProduct> products = new ArrayList<>();
    for (Object[] row : rows) {
      Long id = ((Number) row[0]).longValue();
      String name = (String) row[1];
      double basePriceVal = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
      Money basePrice = new Money(BigDecimal.valueOf(basePriceVal), COP);
      Long categoryId = row[3] != null ? ((Number) row[3]).longValue() : null;
      String categoryName = (String) row[4];
      products.add(new ActiveProduct(id, name, basePrice, categoryId, categoryName));
    }
    return products;
  }

  @Override
  public List<SalesData> loadSalesByProduct(LocalDate start, LocalDate end) {
    String sql =
        """
        SELECT od.product_id, COUNT(*) as units_sold, COALESCE(SUM(od.unit_price), 0) as revenue
        FROM order_details od
        JOIN orders o ON od.order_id = o.id
        WHERE o.date >= :start AND o.date < :end
        GROUP BY od.product_id
        """;

    Query query =
        entityManager
            .createNativeQuery(sql)
            .setParameter("start", start.atStartOfDay())
            .setParameter("end", end.atStartOfDay());

    @SuppressWarnings("unchecked")
    List<Object[]> rows = query.getResultList();

    List<SalesData> result = new ArrayList<>();
    for (Object[] row : rows) {
      Long productId = ((Number) row[0]).longValue();
      int unitsSold = ((Number) row[1]).intValue();
      double revenueVal = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
      result.add(
          new SalesData(productId, unitsSold, new Money(BigDecimal.valueOf(revenueVal), COP)));
    }
    return result;
  }

  @Override
  public Map<Long, Money> loadRecipeCostByProduct() {
    String sql =
        """
        SELECT pr.product_id, COALESCE(SUM(pr.required_quantity * sv.unit_cost), 0) as recipe_cost
        FROM product_recipes pr
        JOIN supply_variants sv ON pr.supply_variant_id = sv.id
        GROUP BY pr.product_id
        """;

    Query query = entityManager.createNativeQuery(sql);

    @SuppressWarnings("unchecked")
    List<Object[]> rows = query.getResultList();

    Map<Long, Money> result = new HashMap<>();
    for (Object[] row : rows) {
      Long productId = ((Number) row[0]).longValue();
      BigDecimal cost = (BigDecimal) row[1];
      result.put(productId, new Money(cost, COP));
    }
    return result;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Selection-mode semantics (Phase D):
   *
   * <ul>
   *   <li>{@code SINGLE_CHOICE} options whose category declares a {@code
   *       replace_supply_category_id} (substitution slot) contribute {@code optionCost −
   *       defaultSlotCost}, where {@code defaultSlotCost} is the base-recipe material cost of the
   *       product for that supply category (fetched from {@link
   *       ProductOptionRepositoryPort#loadDefaultSlotCostByProductAndCategory()}).
   *   <li>{@code REMOVE} options contribute {@code −optionCost}.
   *   <li>{@code EXTRA}, {@code MULTI_CHOICE} and non-replacement {@code SINGLE_CHOICE} options
   *       contribute {@code +optionCost} (current behavior).
   * </ul>
   *
   * <p>The average is taken over the number of distinct {@code (order, product)} pairs in the
   * period (order-detail lines that carried no option contribute zero), preserving the prior
   * per-order averaging semantics.
   */
  @Override
  public Map<Long, Money> loadAvgOptionCostByProduct(LocalDate start, LocalDate end) {
    String optionSql =
        """
        SELECT od.order_id, od.product_id, odo.option_id,
               COALESCE(oc.selection_type, 'SINGLE_CHOICE') AS selection_type,
               oc.replace_supply_category_id,
               COALESCE(SUM(oreq.required_quantity * sv.unit_cost), 0) AS option_cost
        FROM order_details od
        JOIN orders o ON o.id = od.order_id
        JOIN order_detail_options odo ON odo.order_detail_id = od.id
        JOIN product_options po ON po.id = odo.option_id
        LEFT JOIN option_group oc ON oc.id = po.option_category_id
        LEFT JOIN option_recipes oreq ON oreq.option_id = odo.option_id
        LEFT JOIN supply_variants sv ON sv.id = oreq.supply_variant_id
        WHERE o.date >= :start AND o.date < :end
        GROUP BY od.order_id, od.product_id, odo.option_id,
                 oc.selection_type, oc.replace_supply_category_id
        """;

    Query optionQuery =
        entityManager
            .createNativeQuery(optionSql)
            .setParameter("start", start.atStartOfDay())
            .setParameter("end", end.atStartOfDay());
    @SuppressWarnings("unchecked")
    List<Object[]> optionRows = optionQuery.getResultList();

    // Reuse the Phase A default-slot aggregation for substitution contributions.
    Map<Long, Map<Long, Money>> slotCostsByProduct =
        productOptionRepositoryPort.loadDefaultSlotCostByProductAndCategory();

    // Per distinct (order, product) pair, sum the contribution of each selected option.
    Map<Long, Map<Long, BigDecimal>> contributionByOrderProduct = new HashMap<>();
    for (Object[] row : optionRows) {
      Long orderId = toLong(row[0]);
      Long productId = toLong(row[1]);
      if (orderId == null || productId == null) {
        continue;
      }
      BigDecimal optionCost = toBigDecimal(row[5]);
      OptionSelectionType selectionType = normalizeSelectionType(row[3]);
      Long replaceSupplyCategoryId = toLong(row[4]);
      BigDecimal contribution =
          contribution(
              productId, optionCost, selectionType, replaceSupplyCategoryId, slotCostsByProduct);
      contributionByOrderProduct
          .computeIfAbsent(orderId, k -> new HashMap<>())
          .merge(productId, contribution, BigDecimal::add);
    }

    Map<Long, BigDecimal> totalsByProduct = new HashMap<>();
    for (Map<Long, BigDecimal> perOrder : contributionByOrderProduct.values()) {
      for (Map.Entry<Long, BigDecimal> entry : perOrder.entrySet()) {
        totalsByProduct.merge(entry.getKey(), entry.getValue(), BigDecimal::add);
      }
    }

    // Denominator: number of distinct (order, product) pairs in the period (pairs without options
    // contribute zero and still count, matching the previous per-order average).
    Map<Long, Long> orderLineCounts = loadOrderLineCountsByProduct(start, end);

    Map<Long, Money> result = new HashMap<>();
    for (Map.Entry<Long, Long> entry : orderLineCounts.entrySet()) {
      Long productId = entry.getKey();
      long orderLines = entry.getValue();
      if (orderLines <= 0) {
        continue;
      }
      BigDecimal total = totalsByProduct.getOrDefault(productId, BigDecimal.ZERO);
      BigDecimal avg = total.divide(BigDecimal.valueOf(orderLines), 10, RoundingMode.HALF_UP);
      result.put(productId, new Money(avg, COP));
    }
    return result;
  }

  private Map<Long, Long> loadOrderLineCountsByProduct(LocalDate start, LocalDate end) {
    String sql =
        """
        SELECT od.product_id, COUNT(DISTINCT od.order_id) AS order_lines
        FROM order_details od
        JOIN orders o ON o.id = od.order_id
        WHERE o.date >= :start AND o.date < :end
        GROUP BY od.product_id
        """;
    Query query =
        entityManager
            .createNativeQuery(sql)
            .setParameter("start", start.atStartOfDay())
            .setParameter("end", end.atStartOfDay());
    @SuppressWarnings("unchecked")
    List<Object[]> rows = query.getResultList();
    Map<Long, Long> result = new HashMap<>();
    for (Object[] row : rows) {
      Long productId = toLong(row[0]);
      if (productId == null) {
        continue;
      }
      long count = row[1] == null ? 0L : ((Number) row[1]).longValue();
      result.put(productId, count);
    }
    return result;
  }

  private BigDecimal contribution(
      Long productId,
      BigDecimal optionCost,
      OptionSelectionType selectionType,
      Long replaceSupplyCategoryId,
      Map<Long, Map<Long, Money>> slotCostsByProduct) {
    if (selectionType == OptionSelectionType.SINGLE_CHOICE && replaceSupplyCategoryId != null) {
      BigDecimal slotCost = BigDecimal.ZERO;
      Map<Long, Money> productSlots = slotCostsByProduct.get(productId);
      if (productSlots != null) {
        Money slot = productSlots.get(replaceSupplyCategoryId);
        if (slot != null) {
          slotCost = slot.amount();
        }
      }
      return optionCost.subtract(slotCost);
    }
    if (selectionType == OptionSelectionType.REMOVAL) {
      return optionCost.negate();
    }
    return optionCost;
  }

  private static OptionSelectionType normalizeSelectionType(Object value) {
    if (value == null) {
      return OptionSelectionType.SINGLE_CHOICE;
    }
    try {
      return OptionSelectionType.valueOf(value.toString());
    } catch (IllegalArgumentException unknown) {
      return OptionSelectionType.SINGLE_CHOICE;
    }
  }

  private static Long toLong(Object value) {
    return value == null ? null : ((Number) value).longValue();
  }

  private static BigDecimal toBigDecimal(Object value) {
    return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
  }
}
