/* (C) 2026 */
package aros.services.rms.config;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

/**
 * Flyway database migration configuration.
 *
 * <p>Configures Flyway to run database migrations on application startup, including baseline setup
 * for existing databases.
 */
public class FlywayConfig {

  /**
   * Creates and executes Flyway migration pipeline.
   *
   * @param dataSource the database data source
   * @return configured Flyway instance after migration execution
   */
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
