/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.in;

import java.time.LocalDate;

/** Input port for the variance alert generation rules. */
public interface GenerateAlertsUseCase {

  /**
   * Evaluates every alert rule against the most recent data and persists new alerts.
   *
   * <p>Alerts are deduplicated on {@code (type, metric, periodKey)}; re-running the rules never
   * duplicates a row.
   *
   * @param referenceDate the business date used to resolve the current periods
   * @return the number of newly inserted alerts
   */
  int generate(LocalDate referenceDate);
}
