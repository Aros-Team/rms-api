/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.in;

import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import java.math.BigDecimal;
import java.time.LocalTime;

/** Input port for updating the singleton analytics configuration. */
public interface UpdateAnalyticsConfigUseCase {

  /**
   * Carries analytics configuration values requested by an update. Required values, strict
   * start-before-end time ordering, and non-negative thresholds are validated by the application
   * service.
   *
   * @param defaultOpen the default opening time
   * @param defaultClose the default closing time
   * @param lunchStart the lunch period start time
   * @param lunchEnd the lunch period end time
   * @param dinnerStart the dinner period start time
   * @param dinnerEnd the dinner period end time
   * @param foodCostDeviationPp the food cost deviation threshold
   * @param laborCostDeviationPp the labor cost deviation threshold
   * @param salesDropYoyPct the year-over-year sales drop threshold
   * @param updatedBy the identifier of the user requesting the update
   */
  record UpdateAnalyticsConfigCommand(
      LocalTime defaultOpen,
      LocalTime defaultClose,
      LocalTime lunchStart,
      LocalTime lunchEnd,
      LocalTime dinnerStart,
      LocalTime dinnerEnd,
      BigDecimal foodCostDeviationPp,
      BigDecimal laborCostDeviationPp,
      BigDecimal salesDropYoyPct,
      Long updatedBy) {}

  /**
   * Updates the singleton analytics configuration.
   *
   * @param command the requested configuration values
   * @return the updated analytics configuration
   */
  AnalyticsConfig update(UpdateAnalyticsConfigCommand command);
}
