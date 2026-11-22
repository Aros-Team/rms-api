/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.output;

import java.time.ZoneId;

/** Output port exposing the configured business timezone. */
public interface BusinessTimeZoneProvider {

  /**
   * Returns the business timezone.
   *
   * @return the configured timezone, or the bootstrap timezone when none is configured
   */
  ZoneId getZoneId();
}
