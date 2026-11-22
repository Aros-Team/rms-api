/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.in;

import java.util.Map;

/** Input port for an explicit analytics refresh (menu cache, rollups and alerts). */
public interface RefreshAnalyticsUseCase {

  /**
   * Refreshes derived analytics state on demand so a demo never waits for the nightly jobs.
   *
   * @param months number of trailing months to refresh (1..24)
   * @return a summary keyed by operation (for example {@code menuEngineeringPeriods} or {@code
   *     alertsInserted})
   */
  Map<String, Object> refresh(int months);
}
