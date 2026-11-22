/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.exception.PayrollAggregationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Payroll} aggregation math. */
class PayrollTest {

  private static final Currency COP = Currency.getInstance("COP");
  private static final Instant NOW = Instant.parse("2026-08-31T12:00:00Z");

  // ---------------------------------------------------------------------------
  // UC-01: should_add_events_on_top_of_the_manual_totals
  // ---------------------------------------------------------------------------

  @Test
  void should_add_events_on_top_of_the_manual_totals() {
    Payroll payroll = payroll(money("1500000"), money("100000"), money("20000"), money("0"));

    Payroll aggregated = payroll.reaggregate(money("58593.75"), money("75000"), money("0"), NOW);

    assertEquals(0, money("158593.75").compareTo(aggregated.bonuses()));
    assertEquals(0, money("95000").compareTo(aggregated.deductions()));
    assertEquals(0, money("1563593.75").compareTo(aggregated.netAmount()));
    assertEquals(0, money("1563593.75").compareTo(aggregated.pendingAmount()));
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_be_idempotent_when_reaggregated_with_same_inputs
  // ---------------------------------------------------------------------------

  @Test
  void should_be_idempotent_when_reaggregated_with_same_inputs() {
    Payroll payroll = payroll(money("1500000"), money("0"), money("0"), money("0"));

    Payroll first = payroll.reaggregate(money("50000"), money("0"), money("0"), NOW);
    Payroll second = first.reaggregate(money("50000"), money("0"), money("0"), NOW);

    assertEquals(0, first.bonuses().compareTo(second.bonuses()));
    assertEquals(0, first.netAmount().compareTo(second.netAmount()));
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_reject_net_below_paid_amount
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_net_below_paid_amount() {
    Payroll payroll = payroll(money("1000000"), money("0"), money("0"), money("900000"));

    PayrollAggregationException exception =
        assertThrows(
            PayrollAggregationException.class,
            () -> payroll.reaggregate(money("0"), money("200000"), money("900000"), NOW));

    assertEquals(true, exception.getMessage().contains("exceed"));
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_reject_negative_net
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_negative_net() {
    Payroll payroll = payroll(money("100000"), money("0"), money("0"), money("0"));

    assertThrows(
        PayrollAggregationException.class,
        () -> payroll.reaggregate(money("0"), money("200000"), money("0"), NOW));
  }

  // ---------------------------------------------------------------------------
  // UC-05: should_report_immutable_when_paid
  // ---------------------------------------------------------------------------

  @Test
  void should_report_immutable_when_paid() {
    Payroll payroll =
        new Payroll(
            1L,
            1L,
            YearMonth.of(2026, 8),
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 31),
            money("1000000"),
            money("0"),
            money("0"),
            money("0"),
            money("0"),
            money("1000000"),
            money("1000000"),
            new BigDecimal("192"),
            PayrollStatus.PAID,
            null,
            1L,
            NOW,
            NOW);

    assertEquals(true, payroll.isImmutable());
  }

  private Payroll payroll(
      Money base, Money manualBonuses, Money manualDeductions, Money paidAmount) {
    Money bonuses = manualBonuses;
    Money deductions = manualDeductions;
    Money net = base.plus(bonuses).minus(deductions);
    return new Payroll(
        1L,
        1L,
        YearMonth.of(2026, 8),
        LocalDate.of(2026, 8, 1),
        LocalDate.of(2026, 8, 31),
        base,
        manualBonuses,
        manualDeductions,
        bonuses,
        deductions,
        net,
        paidAmount,
        new BigDecimal("192"),
        PayrollStatus.PENDING,
        null,
        1L,
        NOW,
        NOW);
  }

  private Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }
}
