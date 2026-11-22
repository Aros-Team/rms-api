/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.application.service.AlertRuleEvaluator.DogSkuUnits;
import aros.services.rms.core.analytics.application.service.AlertRuleEvaluator.MonthlyCostPoint;
import aros.services.rms.core.analytics.application.service.AlertRuleEvaluator.SalesYoyPoint;
import aros.services.rms.core.analytics.application.service.AlertRuleEvaluator.WeeklyValue;
import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import aros.services.rms.core.analytics.domain.AnalyticsPeriods;
import aros.services.rms.core.analytics.domain.BcgQuadrant;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MenuItemSummary;
import aros.services.rms.core.analytics.domain.MonthlyFinancialSummary;
import aros.services.rms.core.analytics.domain.SystemAlert;
import aros.services.rms.core.analytics.domain.port.in.GenerateAlertsUseCase;
import aros.services.rms.core.analytics.domain.port.in.GetOperationsUseCase;
import aros.services.rms.core.analytics.domain.port.out.AnalyticsConfigRepositoryPort;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringCacheRepositoryPort;
import aros.services.rms.core.analytics.domain.port.out.MonthlyFinancialSummaryRepositoryPort;
import aros.services.rms.core.analytics.domain.port.out.OperationsQueryPort;
import aros.services.rms.core.analytics.domain.port.out.SystemAlertRepositoryPort;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Builds the alert rule inputs from the rollups, live order data and menu cache, then persists the
 * alerts that fire. Insertion is deduplicated on {@code (type, metric, period_key)} so the job is
 * safe to re-run.
 */
@Service
@RequiredArgsConstructor
public class GenerateAlertsService implements GenerateAlertsUseCase {

  private static final int MONTH_BASELINE = 13;
  private static final int REVPASH_WEEKS = 5;
  private static final BigDecimal HUNDRED = new BigDecimal("100");

  private final SystemAlertRepositoryPort alertRepositoryPort;
  private final MonthlyFinancialSummaryRepositoryPort summaryRepositoryPort;
  private final MenuEngineeringCacheRepositoryPort menuCacheRepositoryPort;
  private final AnalyticsConfigRepositoryPort configRepositoryPort;
  private final OperationsQueryPort operationsQueryPort;
  private final GetOperationsUseCase getOperationsUseCase;
  private final Clock clock;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public int generate(LocalDate referenceDate) {
    AnalyticsConfig config = configRepositoryPort.findSingleton();
    Instant detectedAt = Instant.now(clock);

    List<MonthlyCostPoint> monthlyCosts = loadMonthlyCosts(referenceDate);
    List<WeeklyValue> weeklyRevPash = loadWeeklyRevPash(referenceDate);
    List<SalesYoyPoint> salesYoy = loadSalesYoy(referenceDate, config);
    List<DogSkuUnits> dogWeekly = loadDogWeekly(referenceDate);

    List<SystemAlert> candidates =
        AlertRuleEvaluator.evaluate(
            new AlertRuleEvaluator.Input(monthlyCosts, weeklyRevPash, salesYoy, dogWeekly),
            config,
            detectedAt);

    int inserted = 0;
    for (SystemAlert candidate : candidates) {
      if (alertRepositoryPort.insertIfAbsent(candidate)) {
        inserted++;
      }
    }
    return inserted;
  }

  private List<MonthlyCostPoint> loadMonthlyCosts(LocalDate referenceDate) {
    YearMonth current = YearMonth.from(referenceDate);
    String fromKey = current.minusMonths(MONTH_BASELINE - 1L).toString();
    String toKey = current.toString();
    List<MonthlyCostPoint> points = new ArrayList<>();
    for (MonthlyFinancialSummary summary :
        summaryRepositoryPort.findByBucketAndPeriodKeyBetween("monthly", fromKey, toKey)) {
      BigDecimal net =
          summary.getNetSales() == null ? BigDecimal.ZERO : summary.getNetSales().amount();
      if (net.signum() == 0) {
        continue;
      }
      BigDecimal food =
          summary.getCogsFood().amount().multiply(HUNDRED).divide(net, 2, RoundingMode.HALF_UP);
      BigDecimal labor =
          (summary.getLaborTotal() == null ? BigDecimal.ZERO : summary.getLaborTotal().amount())
              .multiply(HUNDRED)
              .divide(net, 2, RoundingMode.HALF_UP);
      points.add(new MonthlyCostPoint(summary.getPeriodKey(), food, labor));
    }
    return points;
  }

  private List<WeeklyValue> loadWeeklyRevPash(LocalDate referenceDate) {
    List<WeeklyValue> values = new ArrayList<>();
    for (int i = REVPASH_WEEKS; i >= 1; i--) {
      LocalDate day = referenceDate.minusWeeks(i);
      String key = AnalyticsPeriods.keyFor("weekly", day);
      var report = getOperationsUseCase.execute("weekly", key, key);
      values.add(new WeeklyValue(key, report.revPash().value().amount()));
    }
    return values;
  }

  private List<SalesYoyPoint> loadSalesYoy(LocalDate referenceDate, AnalyticsConfig config) {
    List<SalesYoyPoint> points = new ArrayList<>();
    YearMonth currentMonth = YearMonth.from(referenceDate);
    LocalDate currentStart = currentMonth.atDay(1);
    LocalDate currentEnd = currentMonth.plusMonths(1).atDay(1);
    LocalDate baselineStart = currentStart.minusYears(1);
    LocalDate baselineEnd = currentEnd.minusYears(1);

    long baseline =
        operationsQueryPort
            .load(
                baselineStart,
                baselineEnd,
                config.lunchStart(),
                config.lunchEnd(),
                config.dinnerStart(),
                config.dinnerEnd())
            .totalOrders();
    if (baseline == 0) {
      return points;
    }
    long current =
        operationsQueryPort
            .load(
                currentStart,
                currentEnd,
                config.lunchStart(),
                config.lunchEnd(),
                config.dinnerStart(),
                config.dinnerEnd())
            .totalOrders();
    points.add(new SalesYoyPoint(currentMonth.toString(), baseline, current));
    return points;
  }

  private List<DogSkuUnits> loadDogWeekly(LocalDate referenceDate) {
    String currentKey = AnalyticsPeriods.keyFor("weekly", referenceDate);
    String previousKey = AnalyticsPeriods.keyFor("weekly", referenceDate.minusWeeks(1));
    List<MenuItemSummary> previous =
        menuCacheRepositoryPort.findByBucketAndPeriodKeyBetween(
            "weekly", previousKey, previousKey, null);
    List<MenuItemSummary> current =
        menuCacheRepositoryPort.findByBucketAndPeriodKeyBetween(
            "weekly", currentKey, currentKey, null);
    if (previous.isEmpty() || current.isEmpty()) {
      return List.of();
    }
    Map<Long, MenuItemSummary> previousByProduct = new HashMap<>();
    for (MenuItemSummary item : previous) {
      if (item.quadrant() == BcgQuadrant.DOG) {
        previousByProduct.put(item.productId(), item);
      }
    }
    List<DogSkuUnits> points = new ArrayList<>();
    for (MenuItemSummary item : current) {
      if (item.quadrant() != BcgQuadrant.DOG) {
        continue;
      }
      MenuItemSummary before = previousByProduct.get(item.productId());
      if (before == null) {
        continue;
      }
      points.add(
          new DogSkuUnits(currentKey, item.productId(), item.productName(), item.unitsSold()));
      points.add(
          new DogSkuUnits(
              previousKey, before.productId(), before.productName(), before.unitsSold()));
    }
    return points;
  }
}
