/* (C) 2026 */

package aros.services.rms.seed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Derives financial summaries and the menu-engineering cache from a finished {@link SeedPlan}. */
final class SeedSummaries {

  private static final BigDecimal HUNDRED = new BigDecimal("100");
  private static final int BATCH = 1_000;

  private final Connection connection;
  private final SeedPlan plan;
  private final LocalDate anchor;
  private final Map<Integer, ProductMeta> products = new LinkedHashMap<>();

  SeedSummaries(Connection connection, SeedPlan plan, LocalDate anchor) {
    this.connection = connection;
    this.plan = plan;
    this.anchor = anchor;
  }

  void write() throws SQLException {
    loadProducts();
    writeFinancialSummaries();
    writeMenuCache();
  }

  private void loadProducts() throws SQLException {
    try (Statement statement = connection.createStatement();
        ResultSet rows =
            statement.executeQuery(
                "SELECT p.id, p.name, p.base_price, p.category_id, c.name AS category_name "
                    + "FROM products p LEFT JOIN categories c ON c.id = p.category_id "
                    + "WHERE p.active = TRUE ORDER BY p.id")) {
      while (rows.next()) {
        products.put(
            rows.getInt("id"),
            new ProductMeta(
                rows.getInt("id"),
                rows.getString("name"),
                rows.getBigDecimal("base_price"),
                rows.getInt("category_id"),
                rows.getString("category_name")));
      }
    }
  }

  private void writeFinancialSummaries() throws SQLException {
    String sql =
        "INSERT INTO monthly_financial_summary (period_key, bucket, net_sales, gross_sales, "
            + "discounts, comped, cogs_food, cogs_beverage, cogs_alcohol, cogs_other, "
            + "food_cogs_pct, labor_foh, labor_boh, labor_total, labor_pct, prime_cost, "
            + "prime_cost_pct, gross_profit_pct, net_profit_pct, data_completeness) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      int pending = 0;
      for (Map.Entry<YearMonth, SeedPlan.MonthAggregate> entry : plan.months().entrySet()) {
        SeedPlan.MonthAggregate month = entry.getValue();
        int activeDays = activeDays(entry.getKey());
        BigDecimal labor =
            prorate(SeedConstants.FULL_MONTH_LABOR, activeDays, entry.getKey().lengthOfMonth());
        BigDecimal laborFoh =
            prorate(SeedConstants.FOH_LABOR, activeDays, entry.getKey().lengthOfMonth());
        BigDecimal laborBoh = labor.subtract(laborFoh);
        String completeness = entry.getKey().equals(YearMonth.from(anchor)) ? "PARTIAL" : "FULL";
        bindSummary(
            statement,
            entry.getKey().toString(),
            "monthly",
            money(month.revenue()),
            month.cost(),
            laborFoh,
            laborBoh,
            completeness);
        statement.addBatch();
        if (++pending >= BATCH) {
          statement.executeBatch();
          pending = 0;
        }
      }

      // Six synthetic months before the window, used by the 12-month alert baselines.
      YearMonth firstSynthetic = YearMonth.from(plan.windowStart()).minusMonths(6);
      for (int i = 0; i < 6; i++) {
        YearMonth month = firstSynthetic.plusMonths(i);
        long revenue = 200_000_000L * SeedConstants.seasonForMonth(month.getMonthValue()) / 100L;
        SeedConstants.Cost cost =
            new SeedConstants.Cost(
                BigDecimal.valueOf(revenue).multiply(new BigDecimal("0.28")),
                BigDecimal.valueOf(revenue).multiply(new BigDecimal("0.02")),
                BigDecimal.valueOf(revenue).multiply(new BigDecimal("0.008")),
                BigDecimal.valueOf(revenue).multiply(new BigDecimal("0.002")));
        bindSummary(
            statement,
            month.toString(),
            "monthly",
            money(revenue),
            cost,
            SeedConstants.FOH_LABOR,
            SeedConstants.BOH_LABOR,
            "PARTIAL");
        statement.addBatch();
      }

      for (Map.Entry<String, WeekBucket> week : weeks().entrySet()) {
        List<SeedPlan.DayAggregate> days =
            week.getValue().days().stream()
                .map(plan.days()::get)
                .filter(java.util.Objects::nonNull)
                .toList();
        long revenue = days.stream().mapToLong(SeedPlan.DayAggregate::revenue).sum();
        SeedConstants.Cost cost = sumCost(days);
        BigDecimal labor = BigDecimal.ZERO;
        for (SeedPlan.DayAggregate day : days) {
          YearMonth month = YearMonth.from(day.date());
          labor =
              labor.add(
                  SeedConstants.FULL_MONTH_LABOR.divide(
                      BigDecimal.valueOf(month.lengthOfMonth()), 2, RoundingMode.HALF_UP));
        }
        BigDecimal laborFoh = labor.multiply(new BigDecimal("0.4298507463"));
        bindSummary(
            statement,
            week.getKey(),
            "weekly",
            money(revenue),
            cost,
            laborFoh.setScale(2, RoundingMode.HALF_UP),
            labor.subtract(laborFoh).setScale(2, RoundingMode.HALF_UP),
            "FULL");
        statement.addBatch();
      }

      LocalDate start = plan.windowStart();
      for (LocalDate day = start; day.isBefore(anchor); day = day.plusDays(1)) {
        SeedPlan.DayAggregate aggregate = plan.days().get(day);
        if (aggregate == null) {
          bindSummary(
              statement,
              day.toString(),
              "daily",
              BigDecimal.ZERO,
              zeroCost(),
              BigDecimal.ZERO,
              BigDecimal.ZERO,
              "EMPTY");
        } else {
          YearMonth month = YearMonth.from(day);
          BigDecimal labor =
              SeedConstants.FULL_MONTH_LABOR.divide(
                  BigDecimal.valueOf(month.lengthOfMonth()), 2, RoundingMode.HALF_UP);
          BigDecimal laborFoh =
              SeedConstants.FOH_LABOR.divide(
                  BigDecimal.valueOf(month.lengthOfMonth()), 2, RoundingMode.HALF_UP);
          bindSummary(
              statement,
              day.toString(),
              "daily",
              money(aggregate.revenue()),
              aggregate.cost(),
              laborFoh,
              labor.subtract(laborFoh),
              "FULL");
        }
        statement.addBatch();
        if (++pending >= BATCH) {
          statement.executeBatch();
          pending = 0;
        }
      }

      long yearRevenue = 0;
      SeedConstants.Cost yearCost = zeroCost();
      for (SeedPlan.DayAggregate day : plan.days().values()) {
        if (day.date().getYear() == anchor.getYear()) {
          yearRevenue += day.revenue();
          yearCost = yearCost.plus(day.cost());
        }
      }
      bindSummary(
          statement,
          Integer.toString(anchor.getYear()),
          "yearly",
          money(yearRevenue),
          yearCost,
          SeedConstants.FOH_LABOR,
          SeedConstants.BOH_LABOR,
          "PARTIAL");
      statement.addBatch();
      statement.executeBatch();
    }
  }

  private void bindSummary(
      PreparedStatement statement,
      String periodKey,
      String bucket,
      BigDecimal netSales,
      SeedConstants.Cost cost,
      BigDecimal laborFoh,
      BigDecimal laborBoh,
      String completeness)
      throws SQLException {
    BigDecimal grossSales = netSales;
    BigDecimal discounts = BigDecimal.ZERO;
    BigDecimal comped = BigDecimal.ZERO;
    BigDecimal totalCogs = cost.total().setScale(2, RoundingMode.HALF_UP);
    BigDecimal laborTotal = laborFoh.add(laborBoh).setScale(2, RoundingMode.HALF_UP);
    BigDecimal prime = totalCogs.add(laborTotal);
    BigDecimal grossProfit = netSales.subtract(totalCogs);
    BigDecimal netProfit = grossProfit.subtract(laborTotal);
    statement.setString(1, periodKey);
    statement.setString(2, bucket);
    statement.setBigDecimal(3, netSales);
    statement.setBigDecimal(4, grossSales);
    statement.setBigDecimal(5, discounts);
    statement.setBigDecimal(6, comped);
    statement.setBigDecimal(7, cost.food().setScale(2, RoundingMode.HALF_UP));
    statement.setBigDecimal(8, cost.beverage().setScale(2, RoundingMode.HALF_UP));
    statement.setBigDecimal(9, cost.alcohol().setScale(2, RoundingMode.HALF_UP));
    statement.setBigDecimal(10, cost.other().setScale(2, RoundingMode.HALF_UP));
    statement.setBigDecimal(11, pct(cost.food(), netSales));
    statement.setBigDecimal(12, laborFoh.setScale(2, RoundingMode.HALF_UP));
    statement.setBigDecimal(13, laborBoh.setScale(2, RoundingMode.HALF_UP));
    statement.setBigDecimal(14, laborTotal);
    statement.setBigDecimal(15, pct(laborTotal, netSales));
    statement.setBigDecimal(16, prime);
    statement.setBigDecimal(17, pct(prime, netSales));
    statement.setBigDecimal(18, pct(grossProfit, netSales));
    statement.setBigDecimal(19, pct(netProfit, netSales));
    statement.setString(20, completeness);
  }

  private void writeMenuCache() throws SQLException {
    String sql =
        "INSERT INTO menu_performance_cache (product_id, product_name, category_id, "
            + "category_name, period_key, bucket, units_sold, revenue, recipe_cost, "
            + "gross_profit_per_unit, total_contribution, quadrant, source_version, updated_at, "
            + "avg_option_cost, effective_cost) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      insertPeriods(statement, menuPeriods());
      statement.executeBatch();
    }
  }

  private List<MenuPeriod> menuPeriods() {
    List<MenuPeriod> periods = new ArrayList<>();
    for (YearMonth month : plan.months().keySet()) {
      periods.add(new MenuPeriod(month.toString(), "monthly", List.of(), month));
    }
    for (Map.Entry<String, WeekBucket> week : weeks().entrySet()) {
      periods.add(new MenuPeriod(week.getKey(), "weekly", week.getValue().days(), null));
    }
    for (int i = 30; i >= 1; i--) {
      LocalDate day = anchor.minusDays(i);
      periods.add(new MenuPeriod(day.toString(), "daily", List.of(day), null));
    }
    return periods;
  }

  private void insertPeriods(PreparedStatement statement, List<MenuPeriod> periods)
      throws SQLException {
    LocalDateTime updatedAt = anchor.atTime(11, 59);
    int pending = 0;
    for (MenuPeriod period : periods) {
      Map<Integer, long[]> sales = salesFor(period);
      List<Integer> volumes = new ArrayList<>();
      List<BigDecimal> margins = new ArrayList<>();
      Map<Integer, BigDecimal> marginByProduct = new LinkedHashMap<>();
      for (ProductMeta product : products.values()) {
        long units = sales.getOrDefault(product.id(), new long[] {0, 0})[0];
        SeedConstants.ProductSpec spec = SeedConstants.PRODUCT_BY_ID.get(product.id());
        BigDecimal margin = product.price().subtract(spec.cost().total()).max(BigDecimal.ZERO);
        marginByProduct.put(product.id(), margin);
        if (units > 0) {
          volumes.add((int) units);
          margins.add(margin);
        }
      }
      volumes.sort(Integer::compareTo);
      margins.sort(BigDecimal::compareTo);
      int medianVolume = volumes.isEmpty() ? 0 : volumes.get(volumes.size() / 2);
      BigDecimal medianMargin =
          margins.isEmpty() ? BigDecimal.ZERO : margins.get(margins.size() / 2);

      for (ProductMeta product : products.values()) {
        final long units = sales.getOrDefault(product.id(), new long[] {0, 0})[0];
        final long revenue = sales.getOrDefault(product.id(), new long[] {0, 0})[1];
        SeedConstants.ProductSpec spec = SeedConstants.PRODUCT_BY_ID.get(product.id());
        final BigDecimal cost = spec.cost().total().setScale(2, RoundingMode.HALF_UP);
        BigDecimal margin = marginByProduct.get(product.id());
        String quadrant;
        if (units == 0) {
          quadrant = "DOG";
        } else if (units >= medianVolume && margin.compareTo(medianMargin) >= 0) {
          quadrant = "STAR";
        } else if (units >= medianVolume) {
          quadrant = "PLOWHORSE";
        } else if (margin.compareTo(medianMargin) >= 0) {
          quadrant = "PUZZLE";
        } else {
          quadrant = "DOG";
        }
        statement.setInt(1, product.id());
        statement.setString(2, product.name());
        if (product.categoryId() == 0) {
          statement.setNull(3, java.sql.Types.BIGINT);
        } else {
          statement.setInt(3, product.categoryId());
        }
        statement.setString(4, product.categoryName());
        statement.setString(5, period.key());
        statement.setString(6, period.bucket());
        statement.setInt(7, (int) units);
        statement.setBigDecimal(8, money(revenue));
        statement.setBigDecimal(9, cost);
        statement.setBigDecimal(10, margin);
        statement.setBigDecimal(11, margin.multiply(BigDecimal.valueOf(units)));
        statement.setString(12, quadrant);
        statement.setString(13, SeedConstants.VERSION);
        statement.setString(14, SeedMath.timestamp(updatedAt));
        statement.setBigDecimal(15, BigDecimal.ZERO);
        statement.setBigDecimal(16, cost);
        statement.addBatch();
        if (++pending >= BATCH) {
          statement.executeBatch();
          pending = 0;
        }
      }
    }
  }

  private Map<Integer, long[]> salesFor(MenuPeriod period) {
    Map<Integer, long[]> sales = new LinkedHashMap<>();
    List<LocalDate> days;
    if (period.month() != null) {
      days = new ArrayList<>();
      for (Map.Entry<LocalDate, Map<Integer, Integer>> entry :
          plan.dailyProductUnits().entrySet()) {
        if (YearMonth.from(entry.getKey()).equals(period.month())) {
          days.add(entry.getKey());
        }
      }
    } else {
      days = period.days();
    }
    for (LocalDate day : days) {
      Map<Integer, Integer> units = plan.dailyProductUnits().get(day);
      if (units == null) {
        continue;
      }
      for (Map.Entry<Integer, Integer> entry : units.entrySet()) {
        long[] values = sales.computeIfAbsent(entry.getKey(), ignored -> new long[2]);
        values[0] += entry.getValue();
        values[1] +=
            (long) SeedConstants.PRODUCT_BY_ID.get(entry.getKey()).price() * entry.getValue();
      }
    }
    return sales;
  }

  private Map<String, WeekBucket> weeks() {
    Map<String, WeekBucket> result = new LinkedHashMap<>();
    WeekFields fields = WeekFields.ISO;
    for (LocalDate day = plan.windowStart(); day.isBefore(anchor); day = day.plusDays(1)) {
      int week = day.get(fields.weekOfWeekBasedYear());
      int year = day.get(fields.weekBasedYear());
      String key = String.format("%d-W%02d", year, week);
      result.computeIfAbsent(key, ignored -> new WeekBucket()).days().add(day);
    }
    return result;
  }

  private int activeDays(YearMonth month) {
    return activeDaysInMonth(month);
  }

  private int activeDaysInMonth(YearMonth month) {
    int count = 0;
    for (LocalDate day = plan.windowStart(); day.isBefore(anchor); day = day.plusDays(1)) {
      if (YearMonth.from(day).equals(month)) {
        count++;
      }
    }
    return Math.max(1, count);
  }

  private BigDecimal prorate(BigDecimal amount, int activeDays, int daysInMonth) {
    return amount
        .multiply(BigDecimal.valueOf(activeDays))
        .divide(BigDecimal.valueOf(daysInMonth), 2, RoundingMode.HALF_UP);
  }

  private SeedConstants.Cost sumCost(List<SeedPlan.DayAggregate> days) {
    SeedConstants.Cost result = zeroCost();
    for (SeedPlan.DayAggregate day : days) {
      result = result.plus(day.cost());
    }
    return result;
  }

  private static BigDecimal pct(BigDecimal numerator, BigDecimal denominator) {
    if (denominator.signum() == 0) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    return numerator.multiply(HUNDRED).divide(denominator, 2, RoundingMode.HALF_UP);
  }

  private static BigDecimal money(long value) {
    return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
  }

  private static SeedConstants.Cost zeroCost() {
    return new SeedConstants.Cost(
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
  }

  private record ProductMeta(
      int id, String name, BigDecimal price, int categoryId, String categoryName) {}

  private record MenuPeriod(String key, String bucket, List<LocalDate> days, YearMonth month) {}

  private static final class WeekBucket {
    private final List<LocalDate> days = new ArrayList<>();

    List<LocalDate> days() {
      return days;
    }
  }
}
