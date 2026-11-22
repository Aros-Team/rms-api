/* (C) 2026 */

package aros.services.rms.core.analytics.application.config;

import aros.services.rms.core.analytics.domain.port.in.GenerateAlertsUseCase;
import aros.services.rms.core.analytics.domain.port.in.RefreshMenuEngineeringUseCase;
import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringCacheRepositoryPort;
import aros.services.rms.core.analytics.domain.port.out.SystemAlertRepositoryPort;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Optional development backfill: when the derived tables are empty after a fresh seed, rebuild the
 * trailing menu cache and the alert list so the analytics screens never start empty.
 *
 * <p>Disabled unless {@code app.analytics.backfill-on-boot=true} (set in the development profile).
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.analytics.backfill-on-boot", havingValue = "true")
public class AnalyticsBootBackfillRunner implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(AnalyticsBootBackfillRunner.class);
  private static final int MONTHS = 13;

  private final MenuEngineeringCacheRepositoryPort menuCacheRepositoryPort;
  private final SystemAlertRepositoryPort alertRepositoryPort;
  private final RefreshMenuEngineeringUseCase refreshMenuEngineeringUseCase;
  private final GenerateAlertsUseCase generateAlertsUseCase;

  /** {@inheritDoc} */
  @Override
  public void run(ApplicationArguments args) {
    try {
      if (menuCacheRepositoryPort.findLatestUpdatedAt() == null) {
        log.info("AnalyticsBootBackfillRunner: menu cache is empty, rebuilding {} months", MONTHS);
        YearMonth current = YearMonth.now();
        for (int i = MONTHS - 1; i >= 0; i--) {
          refreshMenuEngineeringUseCase.refresh("monthly", current.minusMonths(i).toString());
        }
      }
      if (alertRepositoryPort.countAll() == 0) {
        int inserted = generateAlertsUseCase.generate(java.time.LocalDate.now());
        log.info("AnalyticsBootBackfillRunner: generated {} alert(s)", inserted);
      }
    } catch (Exception exception) {
      log.warn(
          "AnalyticsBootBackfillRunner: skipped after failure: {}",
          exception.getMessage(),
          exception);
    }
  }
}
