/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.AlertSeverity;
import aros.services.rms.core.analytics.domain.AlertStatus;
import aros.services.rms.core.analytics.domain.AlertType;
import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import aros.services.rms.core.analytics.domain.SystemAlert;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure variance alert rules evaluated against pre-aggregated metrics.
 *
 * <p>Rules and thresholds (also documented in {@code docs/contracts/analytics.md}):
 *
 * <ul>
 *   <li>{@code FOOD_COST_DEVIATION} — food cost % exceeds its 12-month moving average by more than
 *       {@code foodCostDeviationPp} percentage points → RED.
 *   <li>{@code LABOR_COST_DEVIATION} — labor cost % exceeds its 12-month moving average by more
 *       than {@code laborCostDeviationPp} percentage points → YELLOW.
 *   <li>{@code REVPASH_DROP} — current week RevPASH is more than 15% below its 4-week moving
 *       average → RED.
 *   <li>{@code SALES_DROP_YOY} — order volume is more than {@code salesDropYoyPct} below the same
 *       period one year earlier → YELLOW.
 *   <li>{@code LOW_MARGIN_SKU_SPIKE} — a DOG-quadrant SKU grows more than 50% week over week →
 *       YELLOW.
 * </ul>
 */
public final class AlertRuleEvaluator {

  static final BigDecimal REVPASH_DROP_THRESHOLD_PCT = new BigDecimal("15.00");
  static final BigDecimal DOG_GROWTH_THRESHOLD_PCT = new BigDecimal("50.00");
  static final int COST_BASELINE_MONTHS = 12;
  static final int REVPASH_BASELINE_WEEKS = 4;
  private static final BigDecimal HUNDRED = new BigDecimal("100");

  private AlertRuleEvaluator() {}

  /** One monthly cost observation. */
  public record MonthlyCostPoint(
      String periodKey, BigDecimal foodCostPct, BigDecimal laborCostPct) {}

  /** One weekly observation. */
  public record WeeklyValue(String periodKey, BigDecimal value) {}

  /** Weekly units for one SKU. */
  public record DogSkuUnits(String periodKey, Long productId, String productName, long units) {}

  /** Current vs same-period-last-year order volume. */
  public record SalesYoyPoint(String periodKey, long baselineOrders, long currentOrders) {}

  /** All rule inputs, ordered oldest to newest. */
  public record Input(
      List<MonthlyCostPoint> monthlyCosts,
      List<WeeklyValue> weeklyRevPash,
      List<SalesYoyPoint> salesYoy,
      List<DogSkuUnits> dogWeekly) {}

  /**
   * Evaluates every rule and returns the alerts that fired.
   *
   * @param input the pre-aggregated metrics
   * @param config thresholds and period configuration
   * @param detectedAt detection timestamp stamped on every candidate
   * @return the candidate alerts, empty when no rule fires
   */
  public static List<SystemAlert> evaluate(
      Input input, AnalyticsConfig config, Instant detectedAt) {
    List<SystemAlert> alerts = new ArrayList<>();
    evaluateCostDeviation(
        input.monthlyCosts(),
        true,
        config.foodCostDeviationPp(),
        AlertType.FOOD_COST_DEVIATION,
        AlertSeverity.RED,
        "food_cost_pct",
        "Food cost is %s pp above the 12-month moving average",
        detectedAt,
        alerts);
    evaluateCostDeviation(
        input.monthlyCosts(),
        false,
        config.laborCostDeviationPp(),
        AlertType.LABOR_COST_DEVIATION,
        AlertSeverity.YELLOW,
        "labor_cost_pct",
        "Labor cost is %s pp above the 12-month moving average",
        detectedAt,
        alerts);
    evaluateRevPash(input.weeklyRevPash(), detectedAt, alerts);
    evaluateSalesYoy(input.salesYoy(), config.salesDropYoyPct(), detectedAt, alerts);
    evaluateDogSpike(input.dogWeekly(), detectedAt, alerts);
    return alerts;
  }

  private static void evaluateCostDeviation(
      List<MonthlyCostPoint> points,
      boolean food,
      BigDecimal thresholdPp,
      AlertType type,
      AlertSeverity severity,
      String metric,
      String messageTemplate,
      Instant detectedAt,
      List<SystemAlert> alerts) {
    if (points == null || points.size() < 2) {
      return;
    }
    MonthlyCostPoint current = points.get(points.size() - 1);
    BigDecimal currentValue = food ? current.foodCostPct() : current.laborCostPct();
    if (currentValue == null) {
      return;
    }
    BigDecimal baseline = average(points, food, COST_BASELINE_MONTHS);
    if (baseline.signum() == 0) {
      return;
    }
    BigDecimal deviationPp = currentValue.subtract(baseline);
    if (deviationPp.compareTo(thresholdPp) <= 0) {
      return;
    }
    alerts.add(
        new SystemAlert(
            null,
            severity,
            type,
            metric,
            scale2(baseline),
            scale2(currentValue),
            deviationPct(currentValue, baseline),
            current.periodKey(),
            "monthly",
            String.format(messageTemplate, scale2(deviationPp).toPlainString()),
            detectedAt,
            AlertStatus.OPEN,
            null));
  }

  private static BigDecimal average(List<MonthlyCostPoint> points, boolean food, int lookBack) {
    int endExclusive = points.size() - 1;
    int start = Math.max(0, endExclusive - lookBack);
    List<MonthlyCostPoint> baselinePoints = points.subList(start, endExclusive);
    if (baselinePoints.isEmpty()) {
      return BigDecimal.ZERO;
    }
    BigDecimal sum = BigDecimal.ZERO;
    for (MonthlyCostPoint point : baselinePoints) {
      BigDecimal value = food ? point.foodCostPct() : point.laborCostPct();
      if (value != null) {
        sum = sum.add(value);
      }
    }
    return sum.divide(BigDecimal.valueOf(baselinePoints.size()), 6, RoundingMode.HALF_UP);
  }

  private static void evaluateRevPash(
      List<WeeklyValue> weekly, Instant detectedAt, List<SystemAlert> alerts) {
    if (weekly == null || weekly.size() < 2) {
      return;
    }
    WeeklyValue current = weekly.get(weekly.size() - 1);
    int start = Math.max(0, weekly.size() - 1 - REVPASH_BASELINE_WEEKS);
    List<WeeklyValue> baselinePoints = weekly.subList(start, weekly.size() - 1);
    BigDecimal baseline = averageValues(baselinePoints);
    if (baseline.signum() == 0 || current.value() == null) {
      return;
    }
    BigDecimal change = deviationPct(current.value(), baseline);
    if (change.compareTo(REVPASH_DROP_THRESHOLD_PCT.negate()) >= 0) {
      return;
    }
    alerts.add(
        new SystemAlert(
            null,
            AlertSeverity.RED,
            AlertType.REVPASH_DROP,
            "revpash",
            scale2(baseline),
            scale2(current.value()),
            change,
            current.periodKey(),
            "weekly",
            "RevPASH dropped "
                + change.abs().toPlainString()
                + "% versus the 4-week moving average",
            detectedAt,
            AlertStatus.OPEN,
            null));
  }

  private static void evaluateSalesYoy(
      List<SalesYoyPoint> points,
      BigDecimal thresholdPct,
      Instant detectedAt,
      List<SystemAlert> alerts) {
    if (points == null) {
      return;
    }
    for (SalesYoyPoint point : points) {
      if (point.baselineOrders() <= 0) {
        continue;
      }
      BigDecimal change =
          BigDecimal.valueOf(point.currentOrders() - point.baselineOrders())
              .multiply(HUNDRED)
              .divide(BigDecimal.valueOf(point.baselineOrders()), 2, RoundingMode.HALF_UP);
      if (change.compareTo(thresholdPct.negate()) >= 0) {
        continue;
      }
      alerts.add(
          new SystemAlert(
              null,
              AlertSeverity.YELLOW,
              AlertType.SALES_DROP_YOY,
              "sales_orders",
              BigDecimal.valueOf(point.baselineOrders()),
              BigDecimal.valueOf(point.currentOrders()),
              change,
              point.periodKey(),
              "monthly",
              "Order volume is "
                  + change.abs().toPlainString()
                  + "% below the same period last year",
              detectedAt,
              AlertStatus.OPEN,
              null));
    }
  }

  private static void evaluateDogSpike(
      List<DogSkuUnits> dogWeekly, Instant detectedAt, List<SystemAlert> alerts) {
    if (dogWeekly == null || dogWeekly.isEmpty()) {
      return;
    }
    Map<Long, List<DogSkuUnits>> byProduct = new LinkedHashMap<>();
    for (DogSkuUnits point : dogWeekly) {
      byProduct.computeIfAbsent(point.productId(), key -> new ArrayList<>()).add(point);
    }
    for (List<DogSkuUnits> productPoints : byProduct.values()) {
      productPoints.sort(Comparator.comparing(DogSkuUnits::periodKey));
      if (productPoints.size() < 2) {
        continue;
      }
      DogSkuUnits previous = productPoints.get(productPoints.size() - 2);
      DogSkuUnits current = productPoints.get(productPoints.size() - 1);
      if (previous.units() <= 0) {
        continue;
      }
      BigDecimal change =
          BigDecimal.valueOf(current.units() - previous.units())
              .multiply(HUNDRED)
              .divide(BigDecimal.valueOf(previous.units()), 2, RoundingMode.HALF_UP);
      if (change.compareTo(DOG_GROWTH_THRESHOLD_PCT) <= 0) {
        continue;
      }
      alerts.add(
          new SystemAlert(
              null,
              AlertSeverity.YELLOW,
              AlertType.LOW_MARGIN_SKU_SPIKE,
              "dog_units",
              BigDecimal.valueOf(previous.units()),
              BigDecimal.valueOf(current.units()),
              change,
              current.periodKey(),
              "weekly",
              "Low-margin SKU '"
                  + current.productName()
                  + "' grew "
                  + change.toPlainString()
                  + "% week over week",
              detectedAt,
              AlertStatus.OPEN,
              null));
    }
  }

  private static BigDecimal averageValues(List<WeeklyValue> points) {
    if (points.isEmpty()) {
      return BigDecimal.ZERO;
    }
    BigDecimal sum = BigDecimal.ZERO;
    for (WeeklyValue point : points) {
      if (point.value() != null) {
        sum = sum.add(point.value());
      }
    }
    return sum.divide(BigDecimal.valueOf(points.size()), 6, RoundingMode.HALF_UP);
  }

  private static BigDecimal deviationPct(BigDecimal current, BigDecimal baseline) {
    if (baseline.signum() == 0) {
      return BigDecimal.ZERO;
    }
    return current.subtract(baseline).multiply(HUNDRED).divide(baseline, 2, RoundingMode.HALF_UP);
  }

  private static BigDecimal scale2(BigDecimal value) {
    return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
  }
}
