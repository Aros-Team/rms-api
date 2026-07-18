/* (C) 2026 */

package aros.services.rms.core.analytics.application.config;

import java.time.Clock;
import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Configuration of beans for the analytics module. Exposes a {@link Clock} bean and a ShedLock
 * {@link LockProvider} backed by the JDBC template.
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

  /**
   * Returns a JDBC-backed ShedLock {@link LockProvider} for distributed scheduling locks.
   *
   * @param dataSource the application data source
   * @return the lock provider
   */
  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    return new JdbcTemplateLockProvider(
        JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(dataSource))
            .usingDbTime()
            .build());
  }
}
