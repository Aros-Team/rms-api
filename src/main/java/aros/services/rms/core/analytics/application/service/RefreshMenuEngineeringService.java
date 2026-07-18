/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.BcgQuadrant;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MenuItemSummary;
import aros.services.rms.core.analytics.domain.port.in.RefreshMenuEngineeringUseCase;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringCacheRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link RefreshMenuEngineeringUseCase} that computes BCG quadrant analysis for
 * all active products in a given period using native SQL queries and upserts results into the
 * menu_performance_cache table.
 */
@Service
@RequiredArgsConstructor
public class RefreshMenuEngineeringService implements RefreshMenuEngineeringUseCase {

  private static final Logger log = LoggerFactory.getLogger(RefreshMenuEngineeringService.class);
  private static final Currency COP = Currency.getInstance("COP");
  private static final String SOURCE_VERSION = "v1";

  private final EntityManager entityManager;
  private final MenuEngineeringCacheRepositoryPort cacheRepo;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void refresh(String bucket, String periodKey) {
    log.info("Refreshing menu engineering cache for bucket={} periodKey={}", bucket, periodKey);

    LocalDate periodStart = resolvePeriodStart(bucket, periodKey);
    LocalDate periodEnd = resolvePeriodEnd(bucket, periodKey);

    // 1. Load active products
    List<ProductRow> products = loadActiveProducts();

    if (products.isEmpty()) {
      log.info("No active products found, skipping refresh for {} {}", periodKey, bucket);
      return;
    }

    // 2. Load sales aggregation (units_sold, revenue) per product for the period
    Map<Long, SalesRow> salesByProduct = loadSalesByProduct(periodStart, periodEnd);

    // 3. Load recipe cost per product
    Map<Long, Money> recipeCostByProduct = loadRecipeCostByProduct();

    // 4. Build menu item summaries with preliminary GP
    List<MenuItemSummary> items = new ArrayList<>();
    List<Integer> allVolumes = new ArrayList<>();
    List<BigDecimal> allMargins = new ArrayList<>();

    for (ProductRow product : products) {
      SalesRow sales = salesByProduct.getOrDefault(product.id, new SalesRow(0, BigDecimal.ZERO));
      int unitsSold = sales.unitsSold;
      Money revenue = new Money(sales.revenue, COP);
      Money recipeCost = recipeCostByProduct.getOrDefault(product.id, Money.zero(COP));
      Money sellPrice = product.basePrice;

      Money gpPerUnit = sellPrice.minus(recipeCost);
      if (gpPerUnit.isNegative()) {
        gpPerUnit = Money.zero(COP); // cap at zero — negative GP makes no sense
      }
      Money contribution = gpPerUnit.times(BigDecimal.valueOf(unitsSold));

      MenuItemSummary item =
          new MenuItemSummary(
              product.id,
              product.name,
              product.categoryId,
              product.categoryName,
              unitsSold,
              revenue,
              recipeCost,
              gpPerUnit,
              contribution,
              BcgQuadrant.DOG); // placeholder, set after median

      items.add(item);
      allVolumes.add(unitsSold);
      allMargins.add(gpPerUnit.amount());
    }

    // 5. Compute medians
    int medianVolume = medianInt(allVolumes);
    BigDecimal medianMargin = medianBigDecimal(allMargins);

    // 6. Assign quadrants
    for (MenuItemSummary item : items) {
      BcgQuadrant quadrant = assignQuadrant(item, medianVolume, new Money(medianMargin, COP));
      // Recreate with correct quadrant — need to rebuild since records are immutable
      MenuItemSummary updated =
          new MenuItemSummary(
              item.productId(),
              item.productName(),
              item.categoryId(),
              item.categoryName(),
              item.unitsSold(),
              item.revenue(),
              item.recipeCost(),
              item.grossProfitPerUnit(),
              item.totalContribution(),
              quadrant);
      cacheRepo.upsert(updated, bucket, periodKey, SOURCE_VERSION);
    }

    log.info(
        "Menu engineering refresh complete: {} products processed for {} {}",
        items.size(),
        periodKey,
        bucket);
  }

  // ---------------------------------------------------------------------------
  // Data loading
  // ---------------------------------------------------------------------------

  private List<ProductRow> loadActiveProducts() {
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

    List<ProductRow> products = new ArrayList<>();
    for (Object[] row : rows) {
      Long id = ((Number) row[0]).longValue();
      String name = (String) row[1];
      Double basePriceVal = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
      Money basePrice = new Money(BigDecimal.valueOf(basePriceVal), COP);
      Long categoryId = row[3] != null ? ((Number) row[3]).longValue() : null;
      String categoryName = (String) row[4];
      products.add(new ProductRow(id, name, basePrice, categoryId, categoryName));
    }
    return products;
  }

  private Map<Long, SalesRow> loadSalesByProduct(LocalDate start, LocalDate end) {
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

    Map<Long, SalesRow> result = new HashMap<>();
    for (Object[] row : rows) {
      Long productId = ((Number) row[0]).longValue();
      int unitsSold = ((Number) row[1]).intValue();
      Double revenueVal = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
      result.put(productId, new SalesRow(unitsSold, BigDecimal.valueOf(revenueVal)));
    }
    return result;
  }

  private Map<Long, Money> loadRecipeCostByProduct() {
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

  // ---------------------------------------------------------------------------
  // BCG quadrant logic
  // ---------------------------------------------------------------------------

  static BcgQuadrant assignQuadrant(MenuItemSummary item, int medianVolume, Money medianMargin) {
    boolean highVolume = item.unitsSold() >= medianVolume;
    boolean highMargin = item.grossProfitPerUnit().isGreaterOrEqual(medianMargin);

    if (highVolume && highMargin) {
      return BcgQuadrant.STAR;
    }
    if (highVolume && !highMargin) {
      return BcgQuadrant.PLOWHORSE;
    }
    if (!highVolume && highMargin) {
      return BcgQuadrant.PUZZLE;
    }
    return BcgQuadrant.DOG;
  }

  // ---------------------------------------------------------------------------
  // Median computation (sorted, 0-indexed, upper-middle for even)
  // ---------------------------------------------------------------------------

  static int medianInt(List<Integer> values) {
    if (values.isEmpty()) {
      return 0;
    }
    List<Integer> sorted = new ArrayList<>(values);
    Collections.sort(sorted);
    return sorted.get(sorted.size() / 2);
  }

  static BigDecimal medianBigDecimal(List<BigDecimal> values) {
    if (values.isEmpty()) {
      return BigDecimal.ZERO;
    }
    List<BigDecimal> sorted = new ArrayList<>(values);
    Collections.sort(sorted);
    return sorted.get(sorted.size() / 2);
  }

  // ---------------------------------------------------------------------------
  // Period helpers
  // ---------------------------------------------------------------------------

  private static LocalDate resolvePeriodStart(String bucket, String periodKey) {
    return switch (bucket) {
      case "daily" -> LocalDate.parse(periodKey, DateTimeFormatter.ISO_LOCAL_DATE);
      case "weekly" -> {
        int year = Integer.parseInt(periodKey.substring(0, 4));
        int week = Integer.parseInt(periodKey.substring(6));
        yield LocalDate.ofYearDay(year, 4) // Jan 4 is always in ISO week 1
            .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
            .with(java.time.DayOfWeek.MONDAY);
      }
      case "monthly" -> {
        YearMonth ym = YearMonth.parse(periodKey, DateTimeFormatter.ofPattern("yyyy-MM"));
        yield ym.atDay(1);
      }
      case "yearly" -> LocalDate.of(Integer.parseInt(periodKey), 1, 1);
      default -> throw new IllegalArgumentException("Unsupported bucket: " + bucket);
    };
  }

  private static LocalDate resolvePeriodEnd(String bucket, String periodKey) {
    return switch (bucket) {
      case "daily" -> LocalDate.parse(periodKey, DateTimeFormatter.ISO_LOCAL_DATE).plusDays(1);
      case "weekly" -> {
        LocalDate monday = resolvePeriodStart(bucket, periodKey);
        yield monday.plusDays(7);
      }
      case "monthly" -> {
        YearMonth ym = YearMonth.parse(periodKey, DateTimeFormatter.ofPattern("yyyy-MM"));
        yield ym.plusMonths(1).atDay(1);
      }
      case "yearly" -> LocalDate.of(Integer.parseInt(periodKey) + 1, 1, 1);
      default -> throw new IllegalArgumentException("Unsupported bucket: " + bucket);
    };
  }

  // ---------------------------------------------------------------------------
  // Internal DTOs
  // ---------------------------------------------------------------------------

  record ProductRow(Long id, String name, Money basePrice, Long categoryId, String categoryName) {}

  record SalesRow(int unitsSold, BigDecimal revenue) {}
}
