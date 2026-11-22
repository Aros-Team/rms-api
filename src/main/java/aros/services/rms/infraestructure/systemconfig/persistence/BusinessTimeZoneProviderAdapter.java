/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import aros.services.rms.core.systemconfig.domain.BusinessProfile;
import aros.services.rms.core.systemconfig.domain.port.output.BusinessProfilePort;
import aros.services.rms.core.systemconfig.domain.port.output.BusinessTimeZoneProvider;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Resolves the business timezone from the business profile, falling back to the bootstrap property.
 *
 * <p>The bean value is read during application startup; changing the setting takes effect after the
 * next restart, which keeps existing schedule and time-log calculations stable mid-session.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessTimeZoneProviderAdapter implements BusinessTimeZoneProvider {

  private final BusinessProfilePort businessProfilePort;

  @Value("${app.timezone:America/Bogota}")
  private String bootstrapTimezone;

  /** {@inheritDoc} */
  @Override
  public ZoneId getZoneId() {
    try {
      BusinessProfile profile = businessProfilePort.find().orElse(null);
      String timezone =
          profile != null && profile.getTimezone() != null && !profile.getTimezone().isBlank()
              ? profile.getTimezone()
              : bootstrapTimezone;
      return ZoneId.of(timezone);
    } catch (RuntimeException e) {
      log.warn(
          "Business timezone unavailable ({}), falling back to {}",
          e.getMessage(),
          bootstrapTimezone);
      return ZoneId.of(bootstrapTimezone);
    }
  }
}
