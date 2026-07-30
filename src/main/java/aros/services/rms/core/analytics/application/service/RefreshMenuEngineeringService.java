/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.BcgQuadrant;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MenuItemSummary;
import aros.services.rms.core.analytics.domain.port.in.RefreshMenuEngineeringUseCase;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringAggregationPort;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringAggregationPort.ActiveProduct;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringAggregationPort.SalesData;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringCacheRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
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
 * all active products in a given period using domain aggregation ports and upserts results into the
 * menu_performance_cache table.
 */
@Service
@RequiredArgsConstructor
public class RefreshMenuEngineeringService implements RefreshMenuEngineeringUseCase {

  private static final Logger log = LoggerFactory.getLogger(RefreshMenuEngineeringService.class);
  private static final Currency COP = Currency.getInstance("COP");
  private static final String SOURCE_VERSION = "v1";

  private final MenuEngineeringAggregationPort aggregationPort;
  private final MenuEngineeringCacheRepositoryPort cacheRepo;

  @Override
  @Transactional
  public void refresh(String bucket, String periodKey) {
    log.info("Refreshing menu engineering cache for bucket={} periodKey={}", bucket, periodKey);

    LocalDate periodStart = resolvePeriodStart(bucket, periodKey);
    LocalDate periodEnd = resolvePeriodEnd(bucket, periodKey);

    List<ActiveProduct> products = aggregationPort.loadActiveProducts();

    if (products.isEmpty()) {
      log.info("No active products found, skipping refresh for {} {}", periodKey, bucket);
      return;
    }

    Map<Long, SalesData> salesByProduct = new HashMap<>();
    for (SalesData sd : aggregationPort.loadSalesByProduct(periodStart, periodEnd)) {
      salesByProduct.put(sd.productId(), sd);
    }

    Map<Long, Money> recipeCostByProduct = aggregationPort.loadRecipeCostByProduct();
    Map<Long, Money> avgOptionCostByProduct =
        aggregationPort.loadAvgOptionCostByProduct(periodStart, periodEnd);

    List<MenuItemSummary> items = new ArrayList<>();
    List<Integer> allVolumes = new ArrayList<>();
    List<BigDecimal> allMargins = new ArrayList<>();

    for (ActiveProduct product : products) {
      SalesData sales =
          salesByProduct.getOrDefault(
              product.id(), new SalesData(product.id(), 0, Money.zero(COP)));
      int unitsSold = sales.unitsSold();
      Money revenue = sales.revenue();
      Money recipeCost = recipeCostByProduct.getOrDefault(product.id(), Money.zero(COP));
      Money avgOptionCost = avgOptionCostByProduct.getOrDefault(product.id(), Money.zero(COP));
      Money effectiveCost = recipeCost.plus(avgOptionCost);
      Money sellPrice = product.basePrice();

      Money gpPerUnit = sellPrice.minus(effectiveCost);
      if (gpPerUnit.isNegative()) {
        gpPerUnit = Money.zero(COP);
      }
      Money contribution = gpPerUnit.times(BigDecimal.valueOf(unitsSold));

      MenuItemSummary item =
          new MenuItemSummary(
              product.id(),
              product.name(),
              product.categoryId(),
              product.categoryName(),
              unitsSold,
              revenue,
              recipeCost,
              avgOptionCost,
              effectiveCost,
              gpPerUnit,
              contribution,
              BcgQuadrant.DOG);

      items.add(item);
      allVolumes.add(unitsSold);
      allMargins.add(gpPerUnit.amount());
    }

    int medianVolume = medianInt(allVolumes);
    BigDecimal medianMargin = medianBigDecimal(allMargins);

    for (MenuItemSummary item : items) {
      BcgQuadrant quadrant = assignQuadrant(item, medianVolume, new Money(medianMargin, COP));
      MenuItemSummary updated =
          new MenuItemSummary(
              item.productId(),
              item.productName(),
              item.categoryId(),
              item.categoryName(),
              item.unitsSold(),
              item.revenue(),
              item.recipeCost(),
              item.avgOptionCost(),
              item.effectiveCost(),
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

  private static LocalDate resolvePeriodStart(String bucket, String periodKey) {
    return switch (bucket) {
      case "daily" -> LocalDate.parse(periodKey, DateTimeFormatter.ISO_LOCAL_DATE);
      case "weekly" -> {
        int year = Integer.parseInt(periodKey.substring(0, 4));
        int week = Integer.parseInt(periodKey.substring(6));
        yield LocalDate.ofYearDay(year, 4)
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
}
