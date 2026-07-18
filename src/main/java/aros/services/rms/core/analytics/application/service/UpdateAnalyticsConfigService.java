/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsConfigException;
import aros.services.rms.core.analytics.domain.port.in.UpdateAnalyticsConfigUseCase;
import aros.services.rms.core.analytics.domain.port.out.AnalyticsConfigRepositoryPort;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link UpdateAnalyticsConfigUseCase} that validates non-negative thresholds and
 * strict start-before-end ordering on the time windows, preserves the singleton identifier, and
 * stamps the update timestamp via the injected {@link Clock}.
 */
@Service
@RequiredArgsConstructor
public class UpdateAnalyticsConfigService implements UpdateAnalyticsConfigUseCase {

  private final AnalyticsConfigRepositoryPort repo;
  private final Clock clock;

  /**
   * Updates the singleton analytics configuration with the requested values. Thresholds must be
   * non-negative and each time window must be strictly ordered; otherwise an {@link
   * InvalidAnalyticsConfigException} is raised. The singleton identifier is preserved by reading
   * the current row before saving the rebuilt configuration.
   *
   * @param cmd the requested configuration values
   * @return the updated analytics configuration
   * @throws InvalidAnalyticsConfigException if a threshold is negative or a time window is not
   *     strictly ordered
   */
  @Override
  public AnalyticsConfig update(UpdateAnalyticsConfigCommand cmd) {
    validateThresholds(cmd);
    validateTimeOrdering(cmd);

    AnalyticsConfig current = repo.findSingleton();

    AnalyticsConfig next =
        new AnalyticsConfig(
            current.id(),
            cmd.defaultOpen(),
            cmd.defaultClose(),
            cmd.lunchStart(),
            cmd.lunchEnd(),
            cmd.dinnerStart(),
            cmd.dinnerEnd(),
            cmd.foodCostDeviationPp(),
            cmd.laborCostDeviationPp(),
            cmd.salesDropYoyPct(),
            LocalDateTime.now(clock),
            cmd.updatedBy());

    return repo.save(next);
  }

  private static void validateThresholds(UpdateAnalyticsConfigCommand cmd) {
    if (cmd.foodCostDeviationPp() == null || cmd.foodCostDeviationPp().signum() < 0) {
      throw new InvalidAnalyticsConfigException("foodCostDeviationPp must be >= 0");
    }
    if (cmd.laborCostDeviationPp() == null || cmd.laborCostDeviationPp().signum() < 0) {
      throw new InvalidAnalyticsConfigException("laborCostDeviationPp must be >= 0");
    }
    if (cmd.salesDropYoyPct() == null || cmd.salesDropYoyPct().signum() < 0) {
      throw new InvalidAnalyticsConfigException("salesDropYoyPct must be >= 0");
    }
  }

  private static void validateTimeOrdering(UpdateAnalyticsConfigCommand cmd) {
    if (cmd.defaultOpen() == null
        || cmd.defaultClose() == null
        || !cmd.defaultOpen().isBefore(cmd.defaultClose())) {
      throw new InvalidAnalyticsConfigException("defaultOpen must be strictly before defaultClose");
    }
    if (cmd.lunchStart() == null
        || cmd.lunchEnd() == null
        || !cmd.lunchStart().isBefore(cmd.lunchEnd())) {
      throw new InvalidAnalyticsConfigException("lunchStart must be strictly before lunchEnd");
    }
    if (cmd.dinnerStart() == null
        || cmd.dinnerEnd() == null
        || !cmd.dinnerStart().isBefore(cmd.dinnerEnd())) {
      throw new InvalidAnalyticsConfigException("dinnerStart must be strictly before dinnerEnd");
    }
  }
}
