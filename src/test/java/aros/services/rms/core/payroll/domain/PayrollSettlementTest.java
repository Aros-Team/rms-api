/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PayrollSettlement} record. */
class PayrollSettlementTest {

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
            new BigDecimal("2500000"),
            "August salary",
            "admin");

    assertNotNull(settlement);
    assertNull(settlement.id());
    assertEquals(10L, settlement.payrollId());
    assertEquals(1L, settlement.userId());
    assertEquals(SettlementType.MONTHLY, settlement.settlementType());
    assertEquals(LocalDate.of(2026, 8, 1), settlement.periodStart());
    assertEquals(LocalDate.of(2026, 8, 31), settlement.periodEnd());
    assertEquals(0, new BigDecimal("2500000").compareTo(settlement.amount()));
    assertEquals("August salary", settlement.notes());
    assertNull(settlement.settledAt());
    assertEquals("admin", settlement.settledBy());
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_create_settlement_with_null_notes
  // ---------------------------------------------------------------------------

  @Test
  void should_create_settlement_with_null_notes() {
    PayrollSettlement settlement =
        PayrollSettlement.create(
            5L,
            2L,
            SettlementType.WEEKLY,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 7),
            new BigDecimal("500000"),
            null,
            "manager");

    assertNull(settlement.notes());
    assertNotNull(settlement);
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
            new BigDecimal("1250000"),
            "Half month",
            Instant.parse("2026-08-15T12:00:00Z"),
            "admin");

    assertEquals(42L, settlement.id());
    assertEquals(10L, settlement.payrollId());
    assertEquals(1L, settlement.userId());
    assertEquals(SettlementType.BIWEEKLY, settlement.settlementType());
    assertEquals(LocalDate.of(2026, 8, 1), settlement.periodStart());
    assertEquals(LocalDate.of(2026, 8, 15), settlement.periodEnd());
    assertEquals(0, new BigDecimal("1250000").compareTo(settlement.amount()));
    assertEquals("Half month", settlement.notes());
    assertEquals(Instant.parse("2026-08-15T12:00:00Z"), settlement.settledAt());
    assertEquals("admin", settlement.settledBy());
  }
}
