/* (C) 2026 */
package aros.services.rms.config;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

// @Configuration
public class FlywayConfig {

  // @Bean
  public Flyway flyway(DataSource dataSource) {
    Flyway flyway =
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .connectRetries(10)
            .connectRetriesInterval(2)
            .load();
    flyway.migrate();
    return flyway;
  }
}
