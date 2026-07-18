/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.in;

import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import aros.services.rms.core.analytics.domain.exception.AnalyticsConfigNotFoundException;

/** Input port for retrieving the singleton analytics configuration. */
public interface GetAnalyticsConfigUseCase {

  /**
   * Returns the singleton analytics config. Throws {@link AnalyticsConfigNotFoundException} if the
   * row has been deleted.
   *
   * @return the singleton analytics configuration
   * @throws AnalyticsConfigNotFoundException if the singleton row has been deleted
   */
  AnalyticsConfig get();
}
