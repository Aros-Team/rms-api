/* (C) 2026 */

package aros.services.rms.seed;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Pure, deterministic plan of the seed dataset. No database access happens here.
 *
 * <p>{@link #history(LocalDate)} produces the 179 immutable days; {@link #complete(LocalDate,
 * LocalDateTime)} adds only the orders that can exist at execution time plus the five live orders
 * required by the demo.
 */
final class SeedPlan {

  private final LocalDate anchor;
  private final List<Order> historicalOrders;
  private final List<Order> operationalOrders;
  private final Map<LocalDate, DayAggregate> days = new TreeMap<>();
  private final Map<YearMonth, MonthAggregate> months = new TreeMap<>();
  private final Map<Integer, Integer> productUnits = new LinkedHashMap<>();
  private final Map<Integer, Long> productRevenue = new LinkedHashMap<>();
  private final Map<LocalDate, Map<Integer, Integer>> dailyProductUnits = new LinkedHashMap<>();

  private SeedPlan(LocalDate anchor, List<Order> historicalOrders, List<Order> operationalOrders) {
    this.anchor = anchor;
    this.historicalOrders = List.copyOf(historicalOrders);
    this.operationalOrders = List.copyOf(operationalOrders);
    aggregate();
  }

  static SeedPlan history(LocalDate anchor) {
    return new SeedPlan(anchor, generateHistorical(anchor), List.of());
  }

  static SeedPlan complete(LocalDate anchor, LocalDateTime now) {
    return new SeedPlan(anchor, generateHistorical(anchor), generateOperational(anchor, now));
  }

  LocalDate anchor() {
    return anchor;
  }

  LocalDate windowStart() {
    return anchor.minusDays(SeedConstants.HISTORY_DAYS);
  }

  List<Order> historicalOrders() {
    return historicalOrders;
  }

  List<Order> operationalOrders() {
    return operationalOrders;
  }

  Map<LocalDate, DayAggregate> days() {
    return days;
  }

  Map<YearMonth, MonthAggregate> months() {
    return months;
  }

  int historicalOrdersCount() {
    return historicalOrders.size();
  }

  long historicalLinesCount() {
    return historicalOrders.stream().mapToLong(order -> order.lines().size()).sum();
  }

  long historicalRevenue() {
    return historicalOrders.stream()
        .flatMap(order -> order.lines().stream())
        .mapToLong(Line::unitPrice)
        .sum();
  }

  long historicalCovers() {
    return historicalOrders.stream().mapToLong(Order::partySize).sum();
  }

  long historicalComboOrders() {
    return historicalOrders.stream().filter(Order::combo).count();
  }

  /** Orders that carry a deterministic customer phone (cohort strategy PHONE_OR_TABLE). */
  long historicalPhoneOrders() {
    return historicalOrders.stream()
        .filter(order -> SeedMath.customerPhone(order.id()) != null)
        .count();
  }

  /** Historical orders whose business date falls in {@code [fromInclusive, toExclusive)}. */
  List<Order> historicalOrdersBetween(LocalDate fromInclusive, LocalDate toExclusive) {
    return historicalOrders.stream()
        .filter(order -> !order.openTime().toLocalDate().isBefore(fromInclusive))
        .filter(order -> order.openTime().toLocalDate().isBefore(toExclusive))
        .toList();
  }

  Map<Integer, Integer> productUnits() {
    return productUnits;
  }

  Map<Integer, Long> productRevenue() {
    return productRevenue;
  }

  Map<LocalDate, Map<Integer, Integer>> dailyProductUnits() {
    return dailyProductUnits;
  }

  private void aggregate() {
    for (Order order : historicalOrders) {
      LocalDate date = order.openTime().toLocalDate();
      DayAggregate day = days.computeIfAbsent(date, DayAggregate::new);
      day.orders++;
      MonthAggregate month = months.computeIfAbsent(YearMonth.from(date), MonthAggregate::new);
      month.orders++;
      month.covers += order.partySize();
      Map<Integer, Integer> unitsForDay =
          dailyProductUnits.computeIfAbsent(date, ignored -> new LinkedHashMap<>());
      for (Line line : order.lines()) {
        SeedConstants.ProductSpec product = SeedConstants.PRODUCT_BY_ID.get(line.productId());
        day.lines++;
        day.revenue += line.unitPrice();
        day.cost = day.cost.plus(product.cost());
        month.lines++;
        month.revenue += line.unitPrice();
        month.cost = month.cost.plus(product.cost());
        productUnits.merge(line.productId(), 1, Integer::sum);
        productRevenue.merge(line.productId(), (long) line.unitPrice(), Long::sum);
        unitsForDay.merge(line.productId(), 1, Integer::sum);
      }
      day.covers += order.partySize();
    }
  }

  private static List<Order> generateHistorical(LocalDate anchor) {
    List<Order> orders = new ArrayList<>(17_500);
    long orderId = SeedConstants.HISTORY_ORDER_ID_BASE;
    long detailId = SeedConstants.HISTORY_DETAIL_ID_BASE;
    for (int d = 0; d < SeedConstants.HISTORY_DAYS; d++) {
      int volume = SeedMath.dailyVolume(anchor, d);
      LocalDate date = anchor.minusDays(SeedConstants.HISTORY_DAYS).plusDays(d);
      for (int s = 0; s < volume; s++) {
        orderId++;
        OrderDraft draft = draft(d, s, date, orderId, detailId);
        detailId += draft.lines().size();
        orders.add(draft.order());
      }
    }
    return orders;
  }

  private static List<Order> generateOperational(LocalDate anchor, LocalDateTime now) {
    List<Order> orders = new ArrayList<>();
    long orderId = SeedConstants.OPERATIONAL_ORDER_ID_BASE;
    long detailId = SeedConstants.OPERATIONAL_DETAIL_ID_BASE;
    int d = SeedConstants.HISTORY_DAYS;
    int volume = SeedMath.dailyVolume(anchor, d);
    int accepted = 0;
    for (int s = 0; s < volume; s++) {
      int part = SeedMath.part(d, s);
      LocalDateTime open = orderOpen(anchor, part, d, s);
      int duration = SeedMath.durationMinutes(d, s, part);
      // Only orders that would already be closed at seeding time exist.
      if (open.plusMinutes(duration).isAfter(now) || open.isAfter(now)) {
        continue;
      }
      orderId++;
      OrderDraft draft = draft(d, s, anchor, orderId, detailId);
      detailId += draft.lines().size();
      orders.add(draft.order());
      accepted++;
    }

    // Guarantee a full demo day even when the app is seeded early: fill the
    // remaining count with orders spread over the elapsed service window.
    int minimum = Math.max(SeedConstants.MIN_TODAY_ORDERS, volume);
    int missing = minimum - accepted;
    if (missing > 0) {
      LocalDateTime latest = now.minusMinutes(5);
      LocalDateTime start = anchor.atTime(8, 0);
      if (!start.isBefore(latest)) {
        start = latest.minusMinutes(90);
      }
      if (start.isBefore(anchor.atStartOfDay())) {
        start = anchor.atStartOfDay();
      }
      long span = Math.max(1, Duration.between(start, latest).toMinutes());
      for (int i = 0; i < missing; i++) {
        int s = volume + i;
        int part = SeedMath.part(d, s);
        int duration = SeedMath.durationMinutes(d, s, part);
        long offset = missing == 1 ? 0 : span * i / (missing - 1);
        LocalDateTime open = start.plusMinutes(offset);
        if (open.plusMinutes(duration).isAfter(now)) {
          open = now.minusMinutes(duration + 1L);
        }
        if (open.isBefore(anchor.atStartOfDay())) {
          open = anchor.atStartOfDay();
        }
        orderId++;
        OrderDraft draft = draftAt(d, s, anchor, orderId, detailId, open, duration);
        detailId += draft.lines().size();
        orders.add(draft.order());
      }
    }

    String[] statuses = {"QUEUE", "QUEUE", "PREPARING", "PREPARING", "READY"};
    for (int i = 0; i < 5; i++) {
      orderId++;
      LocalDateTime open = now.minusMinutes(5L + i * 5L);
      int party = 2;
      int lineCount = 2 + SeedMath.liveHash(i, "lines") % 3;
      boolean combo = i == 2;
      List<Line> lines = new ArrayList<>();
      int first = 0;
      if (combo) {
        int product = SeedConstants.COMBO_BASE_ID + SeedMath.liveHash(i, "combo") % 3;
        lines.add(new Line(detailId++, product, SeedConstants.PRODUCT_BY_ID.get(product).price()));
        first = 1;
      }
      for (int k = first; k < lineCount; k++) {
        SeedConstants.ProductSpec product =
            SeedConstants.WEIGHTED_PRODUCTS.get(
                SeedMath.liveHash(i, "product" + k) % SeedConstants.WEIGHTED_PRODUCTS.size());
        lines.add(new Line(detailId++, product.id(), product.price()));
      }
      orders.add(
          new Order(
              orderId,
              anchor,
              open,
              open.plusMinutes(30L + SeedMath.liveHash(i, "dur") % 25L),
              party,
              i + 1,
              statuses[i],
              combo,
              List.copyOf(lines)));
    }
    return orders;
  }

  private static OrderDraft draft(int d, int s, LocalDate date, long orderId, long firstDetailId) {
    int part = SeedMath.part(d, s);
    LocalDateTime open = orderOpen(date, part, d, s);
    int duration = SeedMath.durationMinutes(d, s, part);
    return draftAt(d, s, date, orderId, firstDetailId, open, duration);
  }

  private static OrderDraft draftAt(
      int d,
      int s,
      LocalDate date,
      long orderId,
      long firstDetailId,
      LocalDateTime open,
      int duration) {
    int party = SeedMath.partySize(d, s);
    int table = SeedMath.tableNumber(d, s, party);
    int lineCount = SeedMath.lineCount(d, s);
    boolean combo = SeedMath.isComboOrder(d, s);
    List<Line> lines = new ArrayList<>(lineCount);
    long detailId = firstDetailId;
    int first = 0;
    if (combo) {
      int product = SeedMath.comboProduct(d, s);
      lines.add(new Line(detailId++, product, SeedConstants.PRODUCT_BY_ID.get(product).price()));
      first = 1;
    }
    for (int k = first; k < lineCount; k++) {
      int productId = SeedMath.weightedProduct(d, s, k);
      lines.add(
          new Line(detailId++, productId, SeedConstants.PRODUCT_BY_ID.get(productId).price()));
    }
    return new OrderDraft(
        new Order(
            orderId,
            date,
            open,
            open.plusMinutes(duration),
            party,
            table,
            "DELIVERED",
            combo,
            List.copyOf(lines)),
        List.copyOf(lines));
  }

  private static LocalDateTime orderOpen(LocalDate date, int part, int d, int s) {
    int minuteOfDay =
        switch (part) {
          case 0 -> 11 * 60 + SeedMath.hash("m", d, s) % 240;
          case 1 -> 18 * 60 + SeedMath.hash("m", d, s) % 300;
          default -> 15 * 60 + SeedMath.hash("m", d, s) % 180;
        };
    return date.atStartOfDay().plusMinutes(minuteOfDay);
  }

  /** One order line, with the reserved detail id used by the writer. */
  record Line(long id, int productId, int unitPrice) {}

  /** One order. */
  record Order(
      long id,
      LocalDate anchorDate,
      LocalDateTime openTime,
      LocalDateTime closeTime,
      int partySize,
      int tableNumber,
      String status,
      boolean combo,
      List<Line> lines) {}

  /** Per-day aggregate used by summaries and fixtures. */
  static final class DayAggregate {
    private final LocalDate date;
    private long orders;
    private long lines;
    private long revenue;
    private long covers;
    private SeedConstants.Cost cost = zeroCost();

    DayAggregate(LocalDate date) {
      this.date = date;
    }

    LocalDate date() {
      return date;
    }

    long orders() {
      return orders;
    }

    long lines() {
      return lines;
    }

    long revenue() {
      return revenue;
    }

    long covers() {
      return covers;
    }

    SeedConstants.Cost cost() {
      return cost;
    }
  }

  /** Per-month aggregate used by the financial summaries. */
  static final class MonthAggregate {
    private final YearMonth yearMonth;
    private long orders;
    private long lines;
    private long revenue;
    private long covers;
    private SeedConstants.Cost cost = zeroCost();

    MonthAggregate(YearMonth yearMonth) {
      this.yearMonth = yearMonth;
    }

    YearMonth yearMonth() {
      return yearMonth;
    }

    long orders() {
      return orders;
    }

    long lines() {
      return lines;
    }

    long revenue() {
      return revenue;
    }

    long covers() {
      return covers;
    }

    SeedConstants.Cost cost() {
      return cost;
    }
  }

  private record OrderDraft(Order order, List<Line> lines) {}

  private static SeedConstants.Cost zeroCost() {
    return new SeedConstants.Cost(
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
  }
}
