/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PayrollEvent} record. */
class PayrollEventTest {

  // ---------------------------------------------------------------------------
  // UC-01: should_create_event_with_factory_method
  // ---------------------------------------------------------------------------

  @Test
  void should_create_event_with_factory_method() {
    PayrollEvent event =
        PayrollEvent.create(
            1L,
            LocalDate.of(2026, 8, 1),
            PayrollEventType.OVERTIME,
            new BigDecimal("2.5"),
            new BigDecimal("23437.50"),
            "Weekend extra hours",
            "admin");

    assertNotNull(event);
    assertNull(event.id());
    assertEquals(1L, event.userId());
    assertEquals(LocalDate.of(2026, 8, 1), event.eventDate());
    assertEquals(PayrollEventType.OVERTIME, event.eventType());
    assertEquals(0, new BigDecimal("2.5").compareTo(event.quantity()));
    assertEquals(0, new BigDecimal("23437.50").compareTo(event.unitRate()));
    assertEquals("Weekend extra hours", event.notes());
    assertNull(event.createdAt());
    assertEquals("admin", event.createdBy());
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_calculate_amount_from_quantity_and_rate
  // ---------------------------------------------------------------------------

  @Test
  void should_calculate_amount_from_quantity_and_rate() {
    PayrollEvent event =
        PayrollEvent.create(
            1L,
            LocalDate.of(2026, 8, 1),
            PayrollEventType.OVERTIME,
            new BigDecimal("3"),
            new BigDecimal("10000"),
            null,
            "system");

    // amount = quantity * unitRate = 3 * 10000 = 30000
    assertNotNull(event.amount());
    assertEquals(0, new BigDecimal("30000").compareTo(event.amount()));
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_create_event_with_null_notes
  // ---------------------------------------------------------------------------

  @Test
  void should_create_event_with_null_notes() {
    PayrollEvent event =
        PayrollEvent.create(
            2L,
            LocalDate.of(2026, 9, 15),
            PayrollEventType.DEDUCTION,
            new BigDecimal("1"),
            new BigDecimal("50000"),
            null,
            "manager");

    assertNull(event.notes());
    assertNotNull(event);
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_handle_zero_quantity
  // ---------------------------------------------------------------------------

  @Test
  void should_handle_zero_quantity() {
    PayrollEvent event =
        PayrollEvent.create(
            1L,
            LocalDate.of(2026, 8, 1),
            PayrollEventType.BONUS_PERFORMANCE,
            BigDecimal.ZERO,
            new BigDecimal("100000"),
            "Zero qty bonus",
            "system");

    // amount = 0 * 100000 = 0
    assertEquals(0, BigDecimal.ZERO.compareTo(event.amount()));
  }

  // ---------------------------------------------------------------------------
  // UC-05: should_build_full_record_with_all_fields
  // ---------------------------------------------------------------------------

  @Test
  void should_build_full_record_with_all_fields() {
    PayrollEvent event =
        new PayrollEvent(
            42L,
            7L,
            LocalDate.of(2026, 7, 15),
            PayrollEventType.NIGHT_SURCHARGE,
            new BigDecimal("4"),
            new BigDecimal("27343.75"),
            new BigDecimal("109375"),
            "Night shift",
            java.time.Instant.parse("2026-07-15T10:00:00Z"),
            "admin");

    assertEquals(42L, event.id());
    assertEquals(7L, event.userId());
    assertEquals(LocalDate.of(2026, 7, 15), event.eventDate());
    assertEquals(PayrollEventType.NIGHT_SURCHARGE, event.eventType());
    assertEquals(0, new BigDecimal("4").compareTo(event.quantity()));
    assertEquals(0, new BigDecimal("27343.75").compareTo(event.unitRate()));
    assertEquals(0, new BigDecimal("109375").compareTo(event.amount()));
    assertEquals("Night shift", event.notes());
    assertEquals(java.time.Instant.parse("2026-07-15T10:00:00Z"), event.createdAt());
    assertEquals("admin", event.createdBy());
  }
}
