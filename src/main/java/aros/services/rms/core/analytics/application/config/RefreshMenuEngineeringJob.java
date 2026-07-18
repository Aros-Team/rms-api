/* (C) 2026 */

package aros.services.rms.core.analytics.application.config;

import aros.services.rms.core.analytics.domain.port.in.RefreshMenuEngineeringUseCase;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that runs daily at 03:00 to refresh the menu engineering cache (BCG quadrant
 * analysis) for the last 13 months.
 */
@Component
@RequiredArgsConstructor
public class RefreshMenuEngineeringJob {

  private static final Logger log = LoggerFactory.getLogger(RefreshMenuEngineeringJob.class);

  private final RefreshMenuEngineeringUseCase refreshUseCase;

  /**
   * Refreshes the menu engineering cache for each of the last 13 months. Uses ShedLock to prevent
   * concurrent execution in multi-instance deployments.
   */
  @Scheduled(cron = "0 0 3 * * ?")
  @SchedulerLock(name = "refreshMenuEngineering", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
  public void refreshLast13Months() {
    YearMonth current = YearMonth.now();
    for (int i = 0; i < 13; i++) {
      YearMonth month = current.minusMonths(i);
      String periodKey = month.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
      log.info("RefreshMenuEngineeringJob: refreshing cache for {}", periodKey);
      try {
        refreshUseCase.refresh("monthly", periodKey);
        log.info("RefreshMenuEngineeringJob: completed for {}", periodKey);
      } catch (Exception e) {
        log.error("RefreshMenuEngineeringJob: failed for {}: {}", periodKey, e.getMessage(), e);
      }
    }
  }
}
