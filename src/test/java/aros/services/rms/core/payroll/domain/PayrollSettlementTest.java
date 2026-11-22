/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PayrollSettlement} record. */
class PayrollSettlementTest {

  private static final Currency COP = Currency.getInstance("COP");

  // ---------------------------------------------------------------------------
  // UC-01: should_create_settlement_with_factory_method
  // ---------------------------------------------------------------------------

  @Test
  void should_create_settlement_with_factory_method() {
    PayrollSettlement settlement =
        PayrollSettlement.create(
            10L,
            1L,
            SettlementType.MONTHLY,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 31),
            new Money(new BigDecimal("2500000"), COP),
            "August salary",
            "admin");

    assertNotNull(settlement);
    assertNull(settlement.id());
    assertEquals(10L, settlement.payrollId());
    assertEquals(1L, settlement.userId());
    assertEquals(SettlementType.MONTHLY, settlement.settlementType());
    assertEquals(LocalDate.of(2026, 8, 1), settlement.periodStart());
    assertEquals(LocalDate.of(2026, 8, 31), settlement.periodEnd());
    assertEquals(0, new BigDecimal("2500000").compareTo(settlement.amount().amount()));
    assertEquals("August salary", settlement.notes());
    assertNull(settlement.settledAt());
    assertEquals("admin", settlement.settledBy());
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_detect_overlapping_periods
  // ---------------------------------------------------------------------------

  @Test
  void should_detect_overlapping_periods() {
    PayrollSettlement first = settlement(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 15));
    PayrollSettlement same = settlement(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 15));
    PayrollSettlement touching = settlement(LocalDate.of(2026, 8, 15), LocalDate.of(2026, 8, 31));
    PayrollSettlement disjoint = settlement(LocalDate.of(2026, 8, 16), LocalDate.of(2026, 8, 31));

    assertTrue(first.overlaps(same));
    assertTrue(first.overlaps(touching));
    assertFalse(first.overlaps(disjoint));
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_build_full_settlement_record
  // ---------------------------------------------------------------------------

  @Test
  void should_build_full_settlement_record() {
    PayrollSettlement settlement =
        new PayrollSettlement(
            42L,
            10L,
            1L,
            SettlementType.BIWEEKLY,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 15),
            new Money(new BigDecimal("1250000"), COP),
            "Half month",
            Instant.parse("2026-08-15T12:00:00Z"),
            "admin");

    assertEquals(42L, settlement.id());
    assertEquals(10L, settlement.payrollId());
    assertEquals(1L, settlement.userId());
    assertEquals(SettlementType.BIWEEKLY, settlement.settlementType());
    assertEquals(LocalDate.of(2026, 8, 1), settlement.periodStart());
    assertEquals(LocalDate.of(2026, 8, 15), settlement.periodEnd());
    assertEquals(0, new BigDecimal("1250000").compareTo(settlement.amount().amount()));
    assertEquals("Half month", settlement.notes());
    assertEquals(Instant.parse("2026-08-15T12:00:00Z"), settlement.settledAt());
    assertEquals("admin", settlement.settledBy());
  }

  private PayrollSettlement settlement(LocalDate start, LocalDate end) {
    return PayrollSettlement.create(
        10L,
        1L,
        SettlementType.BIWEEKLY,
        start,
        end,
        new Money(new BigDecimal("100000"), COP),
        null,
        "admin");
  }
}
