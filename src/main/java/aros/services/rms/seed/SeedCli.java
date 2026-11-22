/* (C) 2026 */

package aros.services.rms.seed;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.flywaydb.core.Flyway;

/**
 * Command-line surface of the seed.
 *
 * <p>Invoked by the Gradle tasks {@code seedReset}, {@code seedVerify}, {@code seedMigrate} and
 * {@code seedStatus}; not used by the running application.
 */
public final class SeedCli {

  private SeedCli() {}

  /**
   * Entry point.
   *
   * @param args command plus optional {@code --key=value} pairs
   * @throws Exception when the database cannot be reached or a check fails
   */
  public static void main(String[] args) throws Exception {
    String command = args.length > 0 && !args[0].startsWith("--") ? args[0] : "status";
    Options options = Options.parse(args);
    LocalDate anchor =
        options.anchor().isBlank() ? LocalDate.now() : LocalDate.parse(options.anchor());

    if (options.migrate()) {
      Flyway.configure()
          .dataSource(options.url(), options.user(), options.password())
          .locations("classpath:db/migration")
          .load()
          .migrate();
    }

    try (Connection connection =
        DriverManager.getConnection(options.url(), options.user(), options.password())) {
      switch (command) {
        case "reset" -> reset(connection, anchor, options);
        case "verify" -> verify(connection, anchor);
        case "status" -> status(connection);
        default -> throw new IllegalArgumentException("Unknown command: " + command);
      }
    }
  }

  private static void reset(Connection connection, LocalDate anchor, Options options)
      throws Exception {
    SeedRunner.SeedRunResult result =
        new SeedRunner(connection, anchor, LocalDateTime.now()).reset(!options.skipCatalog());
    System.out.printf(
        "seed reset ok: anchor=%s historicalOrders=%d lines=%d revenue=%d "
            + "covers=%d todayOrders=%d%n",
        result.anchor(),
        result.historicalOrders(),
        result.historicalLines(),
        result.historicalRevenue(),
        result.historicalCovers(),
        result.todayOrders());
    System.out.println("counts: " + countSummary(connection));
  }

  private static void verify(Connection connection, LocalDate anchor) throws Exception {
    List<SeedVerifier.Check> checks = new SeedVerifier(connection, anchor).verify();
    int failed = 0;
    for (SeedVerifier.Check check : checks) {
      if (!check.ok()) {
        failed++;
        System.err.printf(
            "FAIL %-32s expected=%d actual=%d%n", check.name(), check.expected(), check.actual());
      } else {
        System.out.printf("ok   %-32s %d%n", check.name(), check.actual());
      }
    }
    if (failed > 0) {
      throw new IllegalStateException(failed + " seed verification checks failed");
    }
    System.out.println("seed verify ok: " + checks.size() + " checks");
  }

  private static void status(Connection connection) throws Exception {
    System.out.println("counts: " + countSummary(connection));
  }

  private static String countSummary(Connection connection) throws Exception {
    StringBuilder builder = new StringBuilder();
    for (String table :
        List.of(
            "orders",
            "order_details",
            "order_detail_options",
            "inventory_movements",
            "inventory_stock",
            "monthly_financial_summary",
            "menu_performance_cache",
            "payroll",
            "time_logs",
            "system_alerts",
            "cash_shifts",
            "payments",
            "payment_tenders")) {
      try (Statement statement = connection.createStatement();
          ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
        rows.next();
        builder.append(table).append('=').append(rows.getLong(1)).append(' ');
      }
    }
    return builder.toString().trim();
  }

  private record Options(
      String url,
      String user,
      String password,
      String anchor,
      boolean skipCatalog,
      boolean migrate) {

    static Options parse(String[] args) {
      String url =
          System.getProperty(
              "seed.db.url",
              System.getenv()
                  .getOrDefault(
                      "SEED_DB_URL",
                      "jdbc:mysql://localhost:3306/rmsdb"
                          + "?useSSL=false&allowPublicKeyRetrieval=true"
                          + "&rewriteBatchedStatements=true"
                          + "&connectionTimeZone=America/Bogota"
                          + "&forceConnectionTimeZoneToSession=true"));
      String user =
          System.getProperty("seed.db.user", System.getenv().getOrDefault("SEED_DB_USER", "root"));
      String password =
          System.getProperty(
              "seed.db.password", System.getenv().getOrDefault("SEED_DB_PASSWORD", "root"));
      String anchor = "";
      boolean skipCatalog = false;
      boolean skipMigrate = false;
      for (String arg : args) {
        if (arg.startsWith("--url=")) {
          url = arg.substring(6);
        } else if (arg.startsWith("--user=")) {
          user = arg.substring(7);
        } else if (arg.startsWith("--password=")) {
          password = arg.substring(11);
        } else if (arg.startsWith("--anchor=")) {
          anchor = arg.substring(9);
        } else if (arg.equals("--skip-catalog")) {
          skipCatalog = true;
        } else if (arg.equals("--skip-migrate")) {
          skipMigrate = true;
        }
      }
      return new Options(url, user, password, anchor, skipCatalog, !skipMigrate);
    }
  }
}
