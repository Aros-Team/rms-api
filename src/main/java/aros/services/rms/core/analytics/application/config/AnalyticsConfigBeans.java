/* (C) 2026 */

package aros.services.rms.core.analytics.application.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of beans for the analytics module. Exposes a {@link Clock} bean so application
 * services can inject a deterministic time source for unit testing while the production wiring
 * falls back to UTC.
 */
@Configuration
public class AnalyticsConfigBeans {

  /**
   * Returns the {@link Clock} used by analytics services when stamping update timestamps.
   *
   * @return the analytics module clock in UTC
   */
  @Bean
  public Clock analyticsClock() {
    return Clock.systemUTC();
  }
}
