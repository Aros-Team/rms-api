/* (C) 2026 */

package aros.services.rms.core.analytics.application.config;

import aros.services.rms.core.analytics.domain.port.in.RefreshPrimeCostUseCase;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that runs daily at 02:00 to compute the previous day's prime cost aggregates and
 * upserts them into the monthly_financial_summary table.
 */
@Component
@RequiredArgsConstructor
public class RollupDailyJob {

  private static final Logger log = LoggerFactory.getLogger(RollupDailyJob.class);

  private final RefreshPrimeCostUseCase refreshPrimeCost;

  /**
   * Aggregates yesterday's data and upserts into the monthly_financial_summary table. Uses ShedLock
   * to prevent concurrent execution in multi-instance deployments.
   */
  @Scheduled(cron = "0 0 2 * * ?")
  @SchedulerLock(name = "rollupPrimeCost", lockAtLeastFor = "PT5M", lockAtMostFor = "PT10M")
  public void rollupYesterday() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    log.info("RollupDailyJob: aggregating prime cost for {}", yesterday);
    try {
      var result = refreshPrimeCost.refreshForDate(yesterday);
      log.info(
          "RollupDailyJob: completed for {} — primeCost={}, dataCompleteness={}",
          yesterday,
          result.getPrimeCost(),
          result.getDataCompleteness());
    } catch (Exception e) {
      log.error("RollupDailyJob: failed for {}: {}", yesterday, e.getMessage(), e);
    }
  }
}
