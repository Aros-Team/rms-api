/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import aros.services.rms.core.analytics.application.service.AlertRuleEvaluator.DogSkuUnits;
import aros.services.rms.core.analytics.application.service.AlertRuleEvaluator.Input;
import aros.services.rms.core.analytics.application.service.AlertRuleEvaluator.MonthlyCostPoint;
import aros.services.rms.core.analytics.application.service.AlertRuleEvaluator.SalesYoyPoint;
import aros.services.rms.core.analytics.application.service.AlertRuleEvaluator.WeeklyValue;
import aros.services.rms.core.analytics.domain.AlertSeverity;
import aros.services.rms.core.analytics.domain.AlertType;
import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import aros.services.rms.core.analytics.domain.SystemAlert;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AlertRuleEvaluator}.
 *
 * <p><b>Feature:</b> Variance alert rules and severity assignment.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Golden G5: food cost, RevPASH, sales YoY and DOG-spike rules fire
 *   <li>The labor deviation stays below its threshold and produces no alert
 *   <li>Deviation percentages are signed and rounded to two decimals
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Builds 12 baseline months plus the current month with the golden values
 *   <li>Calls the pure evaluator with default analytics thresholds
 *   <li>Asserts the alert count, types, severities and deviations
 * </ul>
 */
class AlertRuleEvaluatorTest {

  @Test
  void should_match_golden_g5_alert_set() {
    List<MonthlyCostPoint> monthly = new ArrayList<>();
    for (int i = 0; i < 12; i++) {
      monthly.add(
          new MonthlyCostPoint(
              "2026-" + String.format("%02d", i + 1),
              new BigDecimal("30.00"),
              new BigDecimal("22.00")));
    }
    monthly.add(new MonthlyCostPoint("2027-01", new BigDecimal("32.70"), new BigDecimal("22.50")));

    List<WeeklyValue> revPash = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      revPash.add(new WeeklyValue("2026-W" + (48 + i), new BigDecimal("12000.00")));
    }
    revPash.add(new WeeklyValue("2027-W01", new BigDecimal("9000.00")));

    Input input =
        new Input(
            monthly,
            revPash,
            List.of(new SalesYoyPoint("2027-01", 100, 86)),
            List.of(
                new DogSkuUnits("2026-W52", 5L, "Dog SKU", 50),
                new DogSkuUnits("2027-W01", 5L, "Dog SKU", 80)));

    List<SystemAlert> alerts =
        AlertRuleEvaluator.evaluate(
            input,
            AnalyticsConfig.defaults(LocalDateTime.of(2027, 1, 31, 0, 0)),
            Instant.parse("2027-02-01T04:00:00Z"));

    assertEquals(4, alerts.size());
    Map<AlertType, SystemAlert> byType =
        alerts.stream().collect(Collectors.toMap(SystemAlert::getType, Function.identity()));

    SystemAlert food = byType.get(AlertType.FOOD_COST_DEVIATION);
    assertEquals(AlertSeverity.RED, food.getSeverity());
    assertEquals("9.00", food.getDeviationPct().toPlainString());
    assertEquals("30.00", food.getBaseline().toPlainString());
    assertEquals("32.70", food.getCurrent().toPlainString());

    SystemAlert revpash = byType.get(AlertType.REVPASH_DROP);
    assertEquals(AlertSeverity.RED, revpash.getSeverity());
    assertEquals("-25.00", revpash.getDeviationPct().toPlainString());

    SystemAlert sales = byType.get(AlertType.SALES_DROP_YOY);
    assertEquals(AlertSeverity.YELLOW, sales.getSeverity());
    assertEquals("-14.00", sales.getDeviationPct().toPlainString());

    SystemAlert dog = byType.get(AlertType.LOW_MARGIN_SKU_SPIKE);
    assertEquals(AlertSeverity.YELLOW, dog.getSeverity());
    assertEquals("60.00", dog.getDeviationPct().toPlainString());

    assertEquals(2, alerts.stream().filter(a -> a.getSeverity() == AlertSeverity.RED).count());
    assertEquals(2, alerts.stream().filter(a -> a.getSeverity() == AlertSeverity.YELLOW).count());
  }
}
