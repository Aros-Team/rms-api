/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.out;

import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import aros.services.rms.core.analytics.domain.exception.AnalyticsConfigNotFoundException;

/** Output port for analytics configuration persistence. */
public interface AnalyticsConfigRepositoryPort {

  /**
   * Returns the analytics configuration row with id 1.
   *
   * @return the singleton analytics configuration
   * @throws AnalyticsConfigNotFoundException if the singleton row does not exist
   */
  AnalyticsConfig findSingleton();

  /**
   * Upserts an analytics configuration.
   *
   * @param config the analytics configuration to persist
   * @return the updated row with a bumped update timestamp
   */
  AnalyticsConfig save(AnalyticsConfig config);
}
