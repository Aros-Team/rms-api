/* (C) 2026 */

package aros.services.rms.core.analytics.application.config;

import aros.services.rms.core.analytics.domain.port.in.RefreshPrimeCostUseCase;
import aros.services.rms.core.analytics.domain.port.in.RollupPrimeCostUseCase;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that runs daily at 02:00 to compute the previous day's prime cost aggregate, upsert
 * it into the monthly_financial_summary table and roll it up into its week, month and year buckets.
 */
@Component
@RequiredArgsConstructor
public class RollupDailyJob {

  private static final Logger log = LoggerFactory.getLogger(RollupDailyJob.class);

  private final RefreshPrimeCostUseCase refreshPrimeCost;
  private final RollupPrimeCostUseCase rollupPrimeCost;

  /**
   * Aggregates yesterday's data, then rebuilds the weekly, monthly and yearly buckets that contain
   * it. Uses ShedLock to prevent concurrent execution in multi-instance deployments.
   */
  @Scheduled(cron = "0 0 2 * * ?")
  @SchedulerLock(name = "rollupPrimeCost", lockAtLeastFor = "PT5M", lockAtMostFor = "PT10M")
  public void rollupYesterday() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    log.info("RollupDailyJob: aggregating prime cost for {}", yesterday);
    try {
      var result = refreshPrimeCost.refreshForDate(yesterday);
      int rolled = rollupPrimeCost.rollup(yesterday, yesterday);
      log.info(
          "RollupDailyJob: completed for {} — primeCost={}, dataCompleteness={}, rollupRows={}",
          yesterday,
          result.getPrimeCost(),
          result.getDataCompleteness(),
          rolled);
    } catch (Exception e) {
      log.error("RollupDailyJob: failed for {}: {}", yesterday, e.getMessage(), e);
    }
  }
}
