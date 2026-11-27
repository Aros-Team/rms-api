/* (C) 2026 */

package aros.services.rms.seed;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Application-facing reset service, enabled only when {@code app.seed.enabled=true}.
 *
 * <p>The HTTP surface is meant for Playwright {@code globalSetup} and local demos; CI and manual
 * runs use {@code ./gradlew seedReset}.
 */
@Service
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class SeedResetService {

  private final DataSource dataSource;
  private final String uploadsDir;
  private final AtomicBoolean running = new AtomicBoolean(false);

  /**
   * Creates the service.
   *
   * @param dataSource application datasource
   * @param uploadsDir local image storage root used to regenerate product media
   */
  public SeedResetService(
      DataSource dataSource, @Value("${app.storage.local.base-dir:./uploads}") String uploadsDir) {
    this.dataSource = dataSource;
    this.uploadsDir = uploadsDir;
  }

  /**
   * Runs a full, idempotent reset.
   *
   * @param anchor business date, or {@code null} for today
   * @param dataset dataset name; only {@code dev} is supported in v1
   * @return produced counts
   */
  public SeedResult reset(String anchor, String dataset) {
    if (dataset != null && !"dev".equals(dataset)) {
      throw new IllegalArgumentException("Conjunto de datos no soportado: " + dataset);
    }
    if (!running.compareAndSet(false, true)) {
      throw new SeedBusyException("Ya hay una carga de datos en ejecución");
    }
    try (Connection connection = dataSource.getConnection()) {
      LocalDate businessDate =
          anchor == null || anchor.isBlank() ? LocalDate.now() : LocalDate.parse(anchor);
      SeedRunner.SeedRunResult result =
          new SeedRunner(connection, businessDate, LocalDateTime.now(), Paths.get(uploadsDir))
              .reset(true);
      return new SeedResult(
          result.anchor().toString(),
          result.historicalOrders(),
          result.historicalLines(),
          result.historicalRevenue(),
          result.historicalCovers(),
          result.todayOrders());
    } catch (SQLException exception) {
      throw new SeedBusyException("Falló el reinicio de datos: " + exception.getMessage());
    } finally {
      running.set(false);
    }
  }

  /**
   * Returns row counts for the seed-owned tables.
   *
   * @return counts keyed by table name
   */
  public Map<String, Long> status() {
    Map<String, Long> counts = new LinkedHashMap<>();
    String[] tables = {
      "orders",
      "order_details",
      "inventory_movements",
      "monthly_financial_summary",
      "menu_performance_cache",
      "payroll",
      "time_logs",
      "system_alerts",
      "cash_shifts",
      "payments"
    };
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      for (String table : tables) {
        try (ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
          rows.next();
          counts.put(table, rows.getLong(1));
        }
      }
      return counts;
    } catch (SQLException exception) {
      throw new SeedBusyException(
          "Falló la consulta de estado de datos: " + exception.getMessage());
    }
  }

  /** Result of a reset. */
  public record SeedResult(
      String anchor,
      int historicalOrders,
      long historicalLines,
      long historicalRevenue,
      long historicalCovers,
      int todayOrders) {}

  /** Thrown when a reset is already running. */
  public static class SeedBusyException extends RuntimeException {
    SeedBusyException(String message) {
      super(message);
    }
  }
}
