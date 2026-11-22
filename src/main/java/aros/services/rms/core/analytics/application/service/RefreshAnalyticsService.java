/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.AnalyticsPeriods;
import aros.services.rms.core.analytics.domain.port.in.GenerateAlertsUseCase;
import aros.services.rms.core.analytics.domain.port.in.RefreshAnalyticsUseCase;
import aros.services.rms.core.analytics.domain.port.in.RefreshMenuEngineeringUseCase;
import aros.services.rms.core.analytics.domain.port.in.RollupPrimeCostUseCase;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * On-demand analytics refresh used by the admin screen and the demo flow.
 *
 * <p>Refreshes the menu-engineering cache for the trailing months, weeks and days, rebuilds the
 * daily-to-higher prime-cost rollups and re-evaluates the alert rules. It is safe to call
 * repeatedly.
 */
@Service
@RequiredArgsConstructor
public class RefreshAnalyticsService implements RefreshAnalyticsUseCase {

  private static final int WEEKS = 8;
  private static final int DAYS = 30;

  private final RefreshMenuEngineeringUseCase refreshMenuEngineeringUseCase;
  private final RollupPrimeCostUseCase rollupPrimeCostUseCase;
  private final GenerateAlertsUseCase generateAlertsUseCase;
  private final Clock clock;

  /** {@inheritDoc} */
  @Override
  public Map<String, Object> refresh(int months) {
    int effectiveMonths = Math.max(1, Math.min(months, 24));
    LocalDate today = LocalDate.now(clock);

    int monthlyPeriods = 0;
    for (int i = effectiveMonths - 1; i >= 0; i--) {
      YearMonth month = YearMonth.from(today).minusMonths(i);
      refreshMenuEngineeringUseCase.refresh("monthly", month.toString());
      monthlyPeriods++;
    }

    int weeklyPeriods = 0;
    for (int i = WEEKS; i >= 0; i--) {
      String key = AnalyticsPeriods.keyFor("weekly", today.minusWeeks(i));
      refreshMenuEngineeringUseCase.refresh("weekly", key);
      weeklyPeriods++;
    }

    int dailyPeriods = 0;
    for (int i = DAYS; i >= 0; i--) {
      refreshMenuEngineeringUseCase.refresh("daily", today.minusDays(i).toString());
      dailyPeriods++;
    }

    LocalDate rollupFrom = YearMonth.from(today).minusMonths(effectiveMonths - 1L).atDay(1);
    int rollupRows = rollupPrimeCostUseCase.rollup(rollupFrom, today);
    int alertsInserted = generateAlertsUseCase.generate(today);

    Map<String, Object> summary = new LinkedHashMap<>();
    summary.put("monthlyPeriods", monthlyPeriods);
    summary.put("weeklyPeriods", weeklyPeriods);
    summary.put("dailyPeriods", dailyPeriods);
    summary.put("rollupRows", rollupRows);
    summary.put("alertsInserted", alertsInserted);
    summary.put("refreshedAt", java.time.Instant.now(clock).toString());
    return summary;
  }
}
