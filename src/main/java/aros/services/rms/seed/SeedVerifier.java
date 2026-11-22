/* (C) 2026 */

package aros.services.rms.seed;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** SQL assertions for a finished seed. Any failed check makes {@code seedVerify} exit non-zero. */
final class SeedVerifier {

  private final Connection connection;
  private final LocalDate anchor;
  private final SeedPlan expected;

  SeedVerifier(Connection connection, LocalDate anchor) {
    this.connection = connection;
    this.anchor = anchor;
    this.expected = SeedPlan.history(anchor);
  }

  List<Check> verify() throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute("SET time_zone = 'America/Bogota'");
    }
    List<Check> checks = new ArrayList<>();
    checks.add(
        check(
            "historical orders",
            expected.historicalOrdersCount(),
            scalar("SELECT COUNT(*) FROM orders WHERE date < '" + anchor + " 00:00:00'")));
    checks.add(
        check(
            "historical lines",
            expected.historicalLinesCount(),
            scalar(
                "SELECT COUNT(*) FROM order_details od JOIN orders o ON o.id = od.order_id "
                    + "WHERE o.date < '"
                    + anchor
                    + " 00:00:00'")));
    checks.add(
        check(
            "historical revenue",
            expected.historicalRevenue(),
            scalar(
                "SELECT COALESCE(SUM(od.unit_price),0) FROM order_details od "
                    + "JOIN orders o ON o.id = od.order_id WHERE o.date < '"
                    + anchor
                    + " 00:00:00'")));
    checks.add(
        check(
            "historical covers",
            expected.historicalCovers(),
            scalar(
                "SELECT COALESCE(SUM(o.party_size),0) FROM orders o WHERE o.date < '"
                    + anchor
                    + " 00:00:00'")));
    checks.add(
        check(
            "combo orders",
            expected.historicalComboOrders(),
            scalar(
                "SELECT COUNT(DISTINCT od.order_id) FROM order_details od "
                    + "JOIN orders o ON o.id = od.order_id WHERE od.product_id BETWEEN 22 AND 24 "
                    + "AND o.date < '"
                    + anchor
                    + " 00:00:00'")));
    checks.add(check("products", 27, scalar("SELECT COUNT(*) FROM products WHERE active = TRUE")));
    checks.add(check("tables", 20, scalar("SELECT COUNT(*) FROM tables")));
    checks.add(check("seats", 74, scalar("SELECT COALESCE(SUM(capacity),0) FROM tables")));
    checks.add(
        check(
            "workers",
            13,
            scalar(
                "SELECT COUNT(*) FROM users WHERE document LIKE 'SEED-1%' AND role = 'WORKER'")));
    checks.add(check("payroll rows", 78, scalar("SELECT COUNT(*) FROM payroll")));
    checks.add(check("payroll events", 78, scalar("SELECT COUNT(*) FROM payroll_events")));
    checks.add(check("salary history", 13, scalar("SELECT COUNT(*) FROM salary_history")));
    checks.add(check("time logs", expectedTimeLogs(), scalar("SELECT COUNT(*) FROM time_logs")));
    checks.add(
        check(
            "financial buckets",
            expectedFinancialRows(),
            scalar("SELECT COUNT(*) FROM monthly_financial_summary")));
    checks.add(
        check(
            "menu cache non-empty",
            true,
            scalar("SELECT COUNT(*) FROM menu_performance_cache") > 0));
    checks.add(
        check(
            "inventory stock",
            4L * scalar("SELECT COUNT(*) FROM supply_variants"),
            scalar("SELECT COUNT(*) FROM inventory_stock")));
    checks.add(
        check(
            "inventory movements non-empty",
            true,
            scalar("SELECT COUNT(*) FROM inventory_movements") > 0));
    checks.add(
        check(
            "occupied tables", 5, scalar("SELECT COUNT(*) FROM tables WHERE status = 'OCCUPIED'")));
    checks.add(
        check(
            "live orders",
            5,
            scalar("SELECT COUNT(*) FROM orders WHERE status IN ('QUEUE','PREPARING','READY')")));
    checks.add(
        check("no future orders", 0, scalar("SELECT COUNT(*) FROM orders WHERE date > NOW()")));
    checks.add(
        check(
            "historical all delivered",
            0,
            scalar(
                "SELECT COUNT(*) FROM orders WHERE status <> 'DELIVERED' AND date < '"
                    + anchor
                    + " 00:00:00'")));
    checks.add(
        check(
            "historical times present",
            0,
            scalar(
                "SELECT COUNT(*) FROM orders WHERE (open_time IS NULL OR close_time IS NULL) "
                    + "AND date < '"
                    + anchor
                    + " 00:00:00'")));
    checks.add(
        check(
            "close after open",
            0,
            scalar(
                "SELECT COUNT(*) FROM orders "
                    + "WHERE close_time IS NOT NULL AND close_time <= open_time")));
    checks.add(
        check(
            "no negative stock",
            0,
            scalar("SELECT COUNT(*) FROM inventory_stock WHERE current_quantity < 0")));
    checks.add(
        check(
            "no orphan details",
            0,
            scalar(
                "SELECT COUNT(*) FROM order_details od LEFT JOIN orders o ON o.id = od.order_id "
                    + "WHERE o.id IS NULL")));
    checks.add(
        check(
            "unique payroll period",
            0,
            scalar(
                "SELECT COUNT(*) FROM (SELECT user_id, period_year, period_month FROM payroll "
                    + "GROUP BY user_id, period_year, period_month HAVING COUNT(*) > 1) x")));
    checks.add(
        check(
            "paid amount present",
            0,
            scalar("SELECT COUNT(*) FROM payroll WHERE status = 'PAID' AND paid_amount = 0")));
    checks.add(
        check(
            "price history matches catalogue",
            0,
            scalar(
                "SELECT COUNT(*) FROM order_details od JOIN products p ON p.id = od.product_id "
                    + "WHERE od.unit_price <> p.base_price")));
    checks.add(
        check(
            "no zero-unit menu rows",
            0,
            scalar("SELECT COUNT(*) FROM menu_performance_cache WHERE units_sold < 0")));
    return checks;
  }

  private long expectedFinancialRows() {
    java.time.temporal.WeekFields fields = java.time.temporal.WeekFields.ISO;
    java.util.Set<String> weeks = new java.util.HashSet<>();
    for (LocalDate day = anchor.minusDays(SeedConstants.HISTORY_DAYS);
        day.isBefore(anchor);
        day = day.plusDays(1)) {
      weeks.add(day.get(fields.weekBasedYear()) + "-" + day.get(fields.weekOfWeekBasedYear()));
    }
    return SeedConstants.HISTORY_DAYS + weeks.size() + expected.months().size() + 6 + 1;
  }

  private long expectedTimeLogs() {
    long shifts = 0;
    LocalDate start = anchor.minusDays(SeedConstants.HISTORY_DAYS);
    java.util.Map<java.time.YearMonth, Integer> active = new java.util.LinkedHashMap<>();
    for (LocalDate day = start; day.isBefore(anchor); day = day.plusDays(1)) {
      active.merge(java.time.YearMonth.from(day), 1, Integer::sum);
    }
    for (java.util.Map.Entry<java.time.YearMonth, Integer> entry : active.entrySet()) {
      shifts +=
          Math.round(20.0 * entry.getValue() / entry.getKey().lengthOfMonth())
              * SeedConstants.WORKERS.size();
    }
    return shifts * 2;
  }

  private long scalar(String sql) throws SQLException {
    try (Statement statement = connection.createStatement();
        ResultSet rows = statement.executeQuery(sql)) {
      rows.next();
      BigDecimal value = rows.getBigDecimal(1);
      return value == null ? 0 : value.longValue();
    }
  }

  private Check check(String name, long expectedValue, long actualValue) {
    return new Check(name, expectedValue, actualValue, expectedValue == actualValue);
  }

  private Check check(String name, boolean expectedValue, boolean actualValue) {
    return new Check(
        name, expectedValue ? 1 : 0, actualValue ? 1 : 0, expectedValue == actualValue);
  }

  /** One assertion. */
  record Check(String name, long expected, long actual, boolean ok) {}
}
