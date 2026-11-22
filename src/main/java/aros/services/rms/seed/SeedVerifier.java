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

    // P2-SEED-13: deterministic customer identity for cohort realism.
    checks.add(
        check(
            "historical phone orders",
            expected.historicalPhoneOrders(),
            scalar(
                "SELECT COUNT(*) FROM orders WHERE date < '"
                    + anchor
                    + " 00:00:00' AND customer_phone IS NOT NULL")));
    checks.add(
        check(
            "historical table-only orders",
            expected.historicalOrdersCount() - expected.historicalPhoneOrders(),
            scalar(
                "SELECT COUNT(*) FROM orders WHERE date < '"
                    + anchor
                    + " 00:00:00' AND customer_phone IS NULL")));
    checks.add(
        check(
            "phone fingerprints consistent",
            0,
            scalar(
                "SELECT COUNT(*) FROM orders WHERE (customer_phone IS NOT NULL "
                    + "AND (customer_fingerprint IS NULL "
                    + "OR customer_fingerprint <> SHA2(customer_phone, 256))) "
                    + "OR (customer_phone IS NULL AND customer_fingerprint IS NOT NULL)")));
    checks.add(
        check(
            "cohort strategy phone or table",
            3,
            scalar(
                "SELECT CASE WHEN SUM(customer_phone IS NOT NULL) = 0 THEN 1 "
                    + "WHEN SUM(customer_phone IS NULL) = 0 THEN 2 ELSE 3 END "
                    + "FROM orders WHERE date >= '"
                    + anchor.minusDays(SeedConstants.PAYMENT_HISTORY_DAYS)
                    + " 00:00:00' AND date < '"
                    + anchor
                    + " 00:00:00'")));

    // P2-SEED-14: alerts must be present right after a reset.
    checks.add(check("alerts present", 5, scalar("SELECT COUNT(*) FROM system_alerts")));
    checks.add(
        check(
            "open alerts", 4, scalar("SELECT COUNT(*) FROM system_alerts WHERE status = 'OPEN'")));
    checks.add(
        check(
            "read alerts", 1, scalar("SELECT COUNT(*) FROM system_alerts WHERE status = 'READ'")));
    checks.add(
        check(
            "open alert types",
            4,
            scalar("SELECT COUNT(DISTINCT type) FROM system_alerts WHERE status = 'OPEN'")));

    // P3-BE-PAY-08/09: payment demo data and reset safety.
    checks.add(
        check(
            "payments",
            expected
                .historicalOrdersBetween(
                    anchor.minusDays(SeedConstants.PAYMENT_HISTORY_DAYS), anchor)
                .size(),
            scalar("SELECT COUNT(*) FROM payments")));
    checks.add(
        check(
            "closed cash shifts",
            SeedConstants.PAYMENT_HISTORY_DAYS,
            scalar("SELECT COUNT(*) FROM cash_shifts WHERE status = 'CLOSED'")));
    checks.add(
        check(
            "open cash shifts",
            1,
            scalar("SELECT COUNT(*) FROM cash_shifts WHERE status = 'OPEN'")));
    checks.add(
        check(
            "open shift float",
            SeedConstants.OPENING_FLOAT.longValue(),
            scalar(
                "SELECT COALESCE(SUM(opening_float),0) FROM cash_shifts WHERE status = 'OPEN'")));
    checks.add(
        check(
            "today orders unpaid",
            0,
            scalar(
                "SELECT COUNT(*) FROM payments p JOIN orders o ON o.id = p.order_id "
                    + "WHERE o.date >= '"
                    + anchor
                    + " 00:00:00'")));
    checks.add(
        check(
            "no double active payments",
            0,
            scalar(
                "SELECT COUNT(*) FROM (SELECT order_id FROM payments "
                    + "WHERE status <> 'VOIDED' GROUP BY order_id HAVING COUNT(*) > 1) x")));
    checks.add(
        check(
            "payment subtotal matches order",
            0,
            scalar(
                "SELECT COUNT(*) FROM payments p WHERE p.status <> 'VOIDED' AND p.subtotal <> "
                    + "(SELECT COALESCE(SUM(od.unit_price),0) FROM order_details od "
                    + "WHERE od.order_id = p.order_id)")));
    checks.add(
        check(
            "payment total arithmetic",
            0,
            scalar(
                "SELECT COUNT(*) FROM payments WHERE total_amount <> "
                    + "subtotal - discount_amount + tip_amount")));
    checks.add(
        check(
            "tenders sum to tendered",
            0,
            scalar(
                "SELECT COUNT(*) FROM payments p WHERE p.status <> 'VOIDED' AND "
                    + "p.tendered_amount <> (SELECT COALESCE(SUM(t.amount),0) "
                    + "FROM payment_tenders t WHERE t.payment_id = p.id)")));
    checks.add(
        check(
            "payment change arithmetic",
            0,
            scalar(
                "SELECT COUNT(*) FROM payments WHERE change_amount <> "
                    + "tendered_amount - total_amount")));
    checks.add(
        check(
            "shift expected cash",
            0,
            scalar(
                "SELECT COUNT(*) FROM cash_shifts s WHERE s.status = 'CLOSED' AND "
                    + "s.expected_cash <> s.opening_float + COALESCE((SELECT SUM(t.amount) "
                    + "FROM payment_tenders t JOIN payments p ON p.id = t.payment_id "
                    + "WHERE p.shift_id = s.id AND p.status <> 'VOIDED' "
                    + "AND t.method = 'CASH'), 0) - COALESCE((SELECT SUM(p.change_amount) "
                    + "FROM payments p WHERE p.shift_id = s.id AND p.status <> 'VOIDED'), 0)")));
    checks.add(
        check(
            "shift difference arithmetic",
            0,
            scalar(
                "SELECT COUNT(*) FROM cash_shifts WHERE status = 'CLOSED' AND "
                    + "difference_amount <> counted_cash - expected_cash")));

    // P4-INV-06/07: deterministic suppliers, purchase orders and a reconciled stock ledger.
    long activeSuppliers =
        SeedConstants.SUPPLIERS.stream().filter(SeedConstants.SupplierSpec::active).count();
    checks.add(
        check(
            "suppliers", SeedConstants.SUPPLIERS.size(), scalar("SELECT COUNT(*) FROM suppliers")));
    checks.add(
        check(
            "active suppliers",
            activeSuppliers,
            scalar("SELECT COUNT(*) FROM suppliers WHERE active = TRUE")));
    checks.add(
        check(
            "purchase orders",
            SeedConstants.PURCHASE_ORDER_COUNT,
            scalar("SELECT COUNT(*) FROM purchase_orders")));
    checks.add(
        check(
            "received purchase orders",
            SeedConstants.RECEIVED_PURCHASE_ORDERS,
            scalar("SELECT COUNT(*) FROM purchase_orders WHERE notes LIKE 'RECIBIDA%'")));
    checks.add(
        check(
            "confirmed purchase orders",
            SeedConstants.CONFIRMED_PURCHASE_ORDERS,
            scalar("SELECT COUNT(*) FROM purchase_orders WHERE notes LIKE 'CONFIRMADA%'")));
    checks.add(
        check(
            "purchase statuses valid",
            0,
            scalar(
                "SELECT COUNT(*) FROM purchase_orders WHERE notes IS NULL "
                    + "OR (notes NOT LIKE 'RECIBIDA%' AND notes NOT LIKE 'CONFIRMADA%')")));
    checks.add(
        check(
            "purchase items present",
            1,
            scalar("SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM purchase_order_items")));
    checks.add(
        check(
            "purchase quantities valid",
            0,
            scalar(
                "SELECT COUNT(*) FROM purchase_order_items WHERE quantity_ordered <= 0 "
                    + "OR quantity_received < 0 OR quantity_received > quantity_ordered")));
    checks.add(
        check(
            "purchase item total matches order",
            0,
            scalar(
                "SELECT COUNT(*) FROM purchase_orders po WHERE po.total_amount <> "
                    + "(SELECT COALESCE(SUM(i.quantity_ordered * i.unit_price),0) "
                    + "FROM purchase_order_items i WHERE i.purchase_order_id = po.id)")));
    checks.add(
        check(
            "purchase money",
            1,
            scalar(
                "SELECT CASE WHEN COALESCE(SUM(total_amount),0) > 0 THEN 1 ELSE 0 END "
                    + "FROM purchase_orders")));
    checks.add(
        check(
            "no orphan purchase items",
            0,
            scalar(
                "SELECT COUNT(*) FROM purchase_order_items i LEFT JOIN purchase_orders po "
                    + "ON po.id = i.purchase_order_id WHERE po.id IS NULL")));
    checks.add(
        check(
            "received quantities applied",
            0,
            scalar(
                "SELECT COUNT(*) FROM purchase_orders po WHERE po.notes LIKE 'RECIBIDA%' "
                    + "AND EXISTS (SELECT 1 FROM purchase_order_items i "
                    + "WHERE i.purchase_order_id = po.id "
                    + "AND i.quantity_received <> i.quantity_ordered)")));
    checks.add(
        check(
            "confirmed quantities pending",
            0,
            scalar(
                "SELECT COUNT(*) FROM purchase_orders po WHERE po.notes LIKE 'CONFIRMADA%' "
                    + "AND EXISTS (SELECT 1 FROM purchase_order_items i "
                    + "WHERE i.purchase_order_id = po.id AND i.quantity_received <> 0)")));
    checks.add(
        check(
            "received orders with stock entry",
            SeedConstants.RECEIVED_PURCHASE_ORDERS,
            scalar(
                "SELECT COUNT(DISTINCT reference_purchase_order_id) FROM inventory_movements "
                    + "WHERE movement_type = 'PURCHASE'")));
    checks.add(
        check(
            "purchase movements match items",
            0,
            scalar(
                "SELECT COUNT(*) FROM (SELECT purchase_order_id, supply_variant_id FROM ("
                    + "SELECT i.purchase_order_id, i.supply_variant_id, "
                    + "i.quantity_received AS qty "
                    + "FROM purchase_order_items i "
                    + "JOIN purchase_orders po ON po.id = i.purchase_order_id "
                    + "WHERE po.notes LIKE 'RECIBIDA%' "
                    + "UNION ALL SELECT m.reference_purchase_order_id, m.supply_variant_id, "
                    + "-m.quantity FROM inventory_movements m "
                    + "WHERE m.movement_type = 'PURCHASE') all_rows "
                    + "GROUP BY purchase_order_id, supply_variant_id "
                    + "HAVING SUM(qty) <> 0) mismatches")));
    checks.add(
        check(
            "no purchase on anchor day",
            0,
            scalar(
                "SELECT COUNT(*) FROM inventory_movements WHERE movement_type = 'PURCHASE' "
                    + "AND created_at >= '"
                    + anchor
                    + " 00:00:00'")));
    checks.add(
        check(
            "stock ledger matches movements",
            0,
            scalar(
                "SELECT COUNT(*) FROM inventory_stock s LEFT JOIN ("
                    + "SELECT supply_variant_id, storage_location_id, SUM(delta) AS net FROM ("
                    + "SELECT supply_variant_id, to_storage_location_id AS storage_location_id, "
                    + "quantity AS delta FROM inventory_movements "
                    + "WHERE to_storage_location_id IS NOT NULL "
                    + "UNION ALL SELECT supply_variant_id, from_storage_location_id, -quantity "
                    + "FROM inventory_movements WHERE from_storage_location_id IS NOT NULL) flows "
                    + "GROUP BY supply_variant_id, storage_location_id) m "
                    + "ON m.supply_variant_id = s.supply_variant_id "
                    + "AND m.storage_location_id = s.storage_location_id "
                    + "WHERE s.current_quantity <> COALESCE(m.net, 0)")));
    checks.add(
        check(
            "transfers cover consumption",
            0,
            scalar(
                "SELECT COUNT(*) FROM (SELECT supply_variant_id, "
                    + "SUM(CASE WHEN movement_type = 'TRANSFER' THEN quantity "
                    + "ELSE -quantity END) AS net "
                    + "FROM inventory_movements "
                    + "WHERE movement_type IN ('TRANSFER','DEDUCTION') "
                    + "GROUP BY supply_variant_id HAVING net < 0) deficits")));
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
