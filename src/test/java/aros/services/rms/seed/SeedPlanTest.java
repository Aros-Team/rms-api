/* (C) 2026 */

package aros.services.rms.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SeedPlanTest}: deterministic planning of the 180-day dataset.
 *
 * <p><b>Feature:</b> the seed plan produces the counts, revenue and distribution invariants in
 * `seed-spec.md` for a fixed anchor.
 *
 * <p><b>How this test works:</b> builds plans for fixed anchors and asserts the totals and
 * invariants without touching a database.
 */
class SeedPlanTest {

  private static final LocalDate ANCHOR = LocalDate.of(2026, 11, 21);

  @Test
  void producesTheReferenceTotals() {
    SeedPlan plan = SeedPlan.history(ANCHOR);

    assertThat(plan.historicalOrdersCount()).isEqualTo(17_135);
    assertThat(plan.historicalLinesCount()).isEqualTo(59_977);
    assertThat(plan.historicalRevenue()).isEqualTo(1_435_464_200L);
    assertThat(plan.historicalCovers()).isEqualTo(46_229);
    assertThat(plan.historicalComboOrders()).isEqualTo(1_428);
  }

  @Test
  void dailyVolumeMatchesTheAppendix() throws Exception {
    JsonNode fixture = fixture();
    int d = 0;
    for (JsonNode expected : fixture.get("dailyOrderCounts")) {
      assertThat(SeedMath.dailyVolume(ANCHOR, d)).as("day %s", d).isEqualTo(expected.asInt());
      d++;
    }
    assertThat(d).isEqualTo(179);
    assertThat(SeedMath.dailyVolume(ANCHOR, 60)).isZero();
    assertThat(SeedMath.dailyVolume(ANCHOR, 61)).isZero();
    assertThat(SeedMath.dailyVolume(ANCHOR, 62)).isZero();
    assertThat(SeedMath.dailyVolume(ANCHOR, 90)).isEqualTo(18);
    assertThat(SeedMath.dailyVolume(ANCHOR, 135)).isEqualTo(220);
  }

  @Test
  void monthlyProfileMatchesTheSpecification() throws Exception {
    JsonNode fixture = fixture();
    SeedPlan plan = SeedPlan.history(ANCHOR);
    for (JsonNode expected : fixture.get("monthly")) {
      YearMonth key = YearMonth.parse(expected.get("key").asText());
      SeedPlan.MonthAggregate month = plan.months().get(key);

      assertThat(month).as("month %s", key).isNotNull();
      assertThat(month.orders()).as("orders %s", key).isEqualTo(expected.get("orders").asLong());
      assertThat(month.lines()).as("lines %s", key).isEqualTo(expected.get("lines").asLong());
      assertThat(month.revenue()).as("revenue %s", key).isEqualTo(expected.get("revenue").asLong());
      assertThat(month.cost().total().setScale(2, java.math.RoundingMode.HALF_UP))
          .as("cogs %s", key)
          .isEqualByComparingTo(expected.get("cogs").asText());
    }
  }

  @Test
  void productMixMatchesTheSpecification() throws Exception {
    JsonNode fixture = fixture();
    SeedPlan plan = SeedPlan.history(ANCHOR);
    for (var entry : fixture.get("productUnits").properties()) {
      int product = Integer.parseInt(entry.getKey());
      assertThat(plan.productUnits().get(product))
          .as("units %s", product)
          .isEqualTo(entry.getValue().asInt());
      assertThat(plan.productRevenue().get(product))
          .as("revenue %s", product)
          .isEqualTo(fixture.get("productRevenue").get(entry.getKey()).asLong());
    }
    assertThat(plan.productUnits().keySet()).hasSize(27);
  }

  @Test
  void partAndPartyDistributionMatchesTheSpecification() throws Exception {
    JsonNode fixture = fixture();
    Map<String, Integer> parts = new LinkedHashMap<>();
    Map<Integer, Integer> parties = new LinkedHashMap<>();
    for (int d = 0; d < SeedConstants.HISTORY_DAYS; d++) {
      for (int s = 0; s < SeedMath.dailyVolume(ANCHOR, d); s++) {
        int part = SeedMath.part(d, s);
        parts.merge(part == 0 ? "LUNCH" : part == 1 ? "DINNER" : "OTHER", 1, Integer::sum);
        parties.merge(SeedMath.partySize(d, s), 1, Integer::sum);
      }
    }
    assertThat(parts.get("LUNCH")).isEqualTo(fixture.get("partCounts").get("LUNCH").asInt());
    assertThat(parts.get("DINNER")).isEqualTo(fixture.get("partCounts").get("DINNER").asInt());
    assertThat(parts.get("OTHER")).isEqualTo(fixture.get("partCounts").get("OTHER").asInt());
    for (var entry : fixture.get("partyCounts").properties()) {
      assertThat(parties.get(Integer.parseInt(entry.getKey()))).isEqualTo(entry.getValue().asInt());
    }
  }

  @Test
  void occupiedMinutesMatchTheSpecification() {
    SeedPlan plan = SeedPlan.history(ANCHOR);
    long occupied = 0;
    long seatMinutes = 0;
    for (SeedPlan.Order order : plan.historicalOrders()) {
      long duration = java.time.Duration.between(order.openTime(), order.closeTime()).toMinutes();
      occupied += duration;
      seatMinutes += duration * order.partySize();
    }
    assertThat(occupied).isEqualTo(1_037_426L);
    assertThat(seatMinutes).isEqualTo(2_801_527L);
  }

  @Test
  void everyOrderIsDeterministicAndSane() {
    SeedPlan first = SeedPlan.history(ANCHOR);
    SeedPlan second = SeedPlan.history(ANCHOR);
    assertThat(first.historicalRevenue()).isEqualTo(second.historicalRevenue());
    assertThat(first.historicalOrdersCount()).isEqualTo(second.historicalOrdersCount());
    for (SeedPlan.Order order : first.historicalOrders()) {
      assertThat(order.lines()).hasSizeBetween(2, 5);
      assertThat(order.partySize()).isBetween(1, 7);
      assertThat(order.closeTime()).isAfter(order.openTime());
      assertThat(order.tableNumber()).isBetween(1, 20);
      assertThat(order.status()).isEqualTo("DELIVERED");
    }
  }

  @Test
  void customerIdentityIsDeterministicAndMostlyPhoneBased() {
    SeedPlan plan = SeedPlan.history(ANCHOR);
    long phones = plan.historicalPhoneOrders();
    double coverage = (double) phones / plan.historicalOrdersCount();

    // G4 uses 20/30 (66.67%); the demo stays in the same band so cohort is PHONE_OR_TABLE.
    assertThat(coverage).isBetween(0.60, 0.72);
    assertThat(phones).isLessThan(plan.historicalOrdersCount());

    SeedPlan second = SeedPlan.history(ANCHOR);
    assertThat(second.historicalPhoneOrders()).isEqualTo(phones);
    for (SeedPlan.Order order : plan.historicalOrders()) {
      String phone = SeedMath.customerPhone(order.id());
      if (phone != null) {
        assertThat(phone).matches("3\\d{9}");
        assertThat(SeedMath.customerFingerprint(phone)).hasSize(64);
      }
    }
  }

  @Test
  void supplierMasterIsCompleteAndDeterministic() {
    assertThat(SeedConstants.SUPPLIERS).hasSize(12);
    assertThat(SeedConstants.SUPPLIERS)
        .extracting(SeedConstants.SupplierSpec::id)
        .doesNotHaveDuplicates();
    assertThat(SeedConstants.SUPPLIERS).filteredOn(supplier -> supplier.active()).hasSize(11);
    assertThat(
            SeedConstants.SUPPLIERS.stream()
                .filter(supplier -> !supplier.active())
                .map(SeedConstants.SupplierSpec::name)
                .toList())
        .containsExactly("Distribuidora Andina de Insumos");

    for (SeedConstants.SupplierSpec supplier : SeedConstants.SUPPLIERS) {
      assertThat(supplier.name()).isNotBlank();
      assertThat(supplier.contact()).contains("+57").contains("·");
    }
    assertThat(SeedConstants.RECEIVED_PURCHASE_ORDERS + SeedConstants.CONFIRMED_PURCHASE_ORDERS)
        .isEqualTo(SeedConstants.PURCHASE_ORDER_COUNT);
  }

  @Test
  void todayAlwaysProducesFullDaysWithoutFutureOrders() {
    LocalDate anchor = LocalDate.of(2026, 11, 22);

    for (int hour : new int[] {9, 12, 15, 21}) {
      LocalDateTime now = anchor.atTime(hour, 30);
      SeedPlan plan = SeedPlan.complete(anchor, now);
      long today = plan.operationalOrders().size();

      assertThat(today).as("orders at %s", now).isGreaterThanOrEqualTo(80);
      for (SeedPlan.Order order : plan.operationalOrders()) {
        assertThat(order.openTime().toLocalDate()).isEqualTo(anchor);
        assertThat(order.openTime()).as("not in the future at %s", now).isBeforeOrEqualTo(now);
        if ("DELIVERED".equals(order.status())) {
          assertThat(order.closeTime()).as("closed by %s", now).isBeforeOrEqualTo(now);
        }
        assertThat(order.status()).isIn("DELIVERED", "QUEUE", "PREPARING", "READY");
      }
    }
  }

  @Test
  void todayPlanIsDeterministicForTheSameMoment() {
    LocalDateTime now = LocalDate.of(2026, 11, 22).atTime(15, 30);

    SeedPlan first = SeedPlan.complete(LocalDate.of(2026, 11, 22), now);
    SeedPlan second = SeedPlan.complete(LocalDate.of(2026, 11, 22), now);

    assertThat(first.operationalOrders()).hasSameSizeAs(second.operationalOrders());
    assertThat(
            first.operationalOrders().stream()
                .mapToLong(
                    order -> Duration.between(order.openTime(), order.closeTime()).toMinutes())
                .sum())
        .isEqualTo(
            second.operationalOrders().stream()
                .mapToLong(
                    order -> Duration.between(order.openTime(), order.closeTime()).toMinutes())
                .sum());
  }

  private JsonNode fixture() throws Exception {
    try (InputStream stream =
        getClass().getClassLoader().getResourceAsStream("seed/golden/expected.json")) {
      return new ObjectMapper().readTree(stream);
    }
  }
}
