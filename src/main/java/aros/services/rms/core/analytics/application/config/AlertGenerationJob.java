/* (C) 2026 */

package aros.services.rms.core.analytics.application.config;

import aros.services.rms.core.analytics.domain.port.in.GenerateAlertsUseCase;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that runs daily at 04:00, after the prime cost and menu cache jobs, to evaluate the
 * variance alert rules. Inserts are deduplicated, so overlapping runs are harmless.
 */
@Component
@RequiredArgsConstructor
public class AlertGenerationJob {

  private static final Logger log = LoggerFactory.getLogger(AlertGenerationJob.class);

  private final GenerateAlertsUseCase generateAlertsUseCase;

  /** Evaluates every alert rule for the current business date. */
  @Scheduled(cron = "0 0 4 * * ?")
  @SchedulerLock(name = "generateVarianceAlerts", lockAtLeastFor = "PT5M", lockAtMostFor = "PT15M")
  public void generate() {
    LocalDate today = LocalDate.now();
    log.info("AlertGenerationJob: evaluating rules for {}", today);
    try {
      int inserted = generateAlertsUseCase.generate(today);
      log.info("AlertGenerationJob: {} new alert(s) inserted", inserted);
    } catch (Exception e) {
      log.error("AlertGenerationJob: failed for {}: {}", today, e.getMessage(), e);
    }
  }
}
