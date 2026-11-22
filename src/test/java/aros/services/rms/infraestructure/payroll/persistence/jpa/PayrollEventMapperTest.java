/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.PayrollEventType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PayrollEventMapper}. */
class PayrollEventMapperTest {

  private static final Currency COP = Currency.getInstance("COP");
  private final PayrollEventMapper mapper = new PayrollEventMapper(() -> COP);

  // ---------------------------------------------------------------------------
  // UC-01: should_map_entity_to_domain
  // ---------------------------------------------------------------------------

  @Test
  void should_map_entity_to_domain() {
    PayrollEventEntity entity =
        PayrollEventEntity.builder()
            .id(1L)
            .userId(1L)
            .eventDate(LocalDate.of(2026, 8, 1))
            .eventType("OVERTIME")
            .quantity(new BigDecimal("2.5"))
            .unitRate(new BigDecimal("23437.50"))
            .amount(new BigDecimal("58593.75"))
            .notes("Weekend extra")
            .createdAt(Instant.parse("2026-08-01T10:00:00Z"))
            .createdBy("admin")
            .build();

    PayrollEvent domain = mapper.toDomain(entity);

    assertNotNull(domain);
    assertEquals(1L, domain.id());
    assertEquals(1L, domain.userId());
    assertEquals(LocalDate.of(2026, 8, 1), domain.eventDate());
    assertEquals(PayrollEventType.OVERTIME, domain.eventType());
    assertEquals(0, new BigDecimal("2.5").compareTo(domain.quantity()));
    assertEquals(0, new BigDecimal("23437.50").compareTo(domain.unitRate().amount()));
    assertEquals(COP, domain.unitRate().currency());
    assertEquals(0, new BigDecimal("58593.75").compareTo(domain.amount().amount()));
    assertEquals("Weekend extra", domain.notes());
    assertEquals(Instant.parse("2026-08-01T10:00:00Z"), domain.createdAt());
    assertEquals("admin", domain.createdBy());
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_map_domain_to_entity
  // ---------------------------------------------------------------------------

  @Test
  void should_map_domain_to_entity() {
    PayrollEvent domain =
        new PayrollEvent(
            2L,
            3L,
            LocalDate.of(2026, 8, 15),
            PayrollEventType.DEDUCTION,
            new BigDecimal("1"),
            money("50000"),
            money("-50000"),
            "Missing uniform",
            Instant.parse("2026-08-15T14:30:00Z"),
            "manager");

    PayrollEventEntity entity = mapper.toEntity(domain);

    assertNotNull(entity);
    assertEquals(2L, entity.getId());
    assertEquals(3L, entity.getUserId());
    assertEquals(LocalDate.of(2026, 8, 15), entity.getEventDate());
    assertEquals("DEDUCTION", entity.getEventType());
    assertEquals(0, new BigDecimal("1").compareTo(entity.getQuantity()));
    assertEquals(0, new BigDecimal("50000").compareTo(entity.getUnitRate()));
    assertEquals(0, new BigDecimal("-50000").compareTo(entity.getAmount()));
    assertEquals("Missing uniform", entity.getNotes());
    assertEquals("manager", entity.getCreatedBy());
  }

  // ---------------------------------------------------------------------------
  // UC-03: should_handle_null_entity
  // ---------------------------------------------------------------------------

  @Test
  void should_handle_null_entity() {
    assertNull(mapper.toDomain(null));
  }

  // ---------------------------------------------------------------------------
  // UC-04: should_handle_null_domain
  // ---------------------------------------------------------------------------

  @Test
  void should_handle_null_domain() {
    assertNull(mapper.toEntity(null));
  }

  // ---------------------------------------------------------------------------
  // UC-05: should_roundtrip_domain_to_entity_to_domain
  // ---------------------------------------------------------------------------

  @Test
  void should_roundtrip_domain_to_entity_to_domain() {
    PayrollEvent original =
        new PayrollEvent(
            10L,
            5L,
            LocalDate.of(2026, 9, 10),
            PayrollEventType.BONUS_ATTENDANCE,
            new BigDecimal("1"),
            money("7812.50"),
            money("7812.50"),
            "Perfect attendance",
            Instant.parse("2026-09-10T09:00:00Z"),
            "hr");

    PayrollEventEntity entity = mapper.toEntity(original);
    PayrollEvent roundtripped = mapper.toDomain(entity);

    assertNotNull(roundtripped);
    assertEquals(original.id(), roundtripped.id());
    assertEquals(original.userId(), roundtripped.userId());
    assertEquals(original.eventDate(), roundtripped.eventDate());
    assertEquals(original.eventType(), roundtripped.eventType());
    assertEquals(0, original.quantity().compareTo(roundtripped.quantity()));
    assertEquals(0, original.unitRate().amount().compareTo(roundtripped.unitRate().amount()));
    assertEquals(0, original.amount().amount().compareTo(roundtripped.amount().amount()));
    assertEquals(original.notes(), roundtripped.notes());
    assertEquals(original.createdBy(), roundtripped.createdBy());
  }

  // ---------------------------------------------------------------------------
  // UC-06: should_handle_null_notes_in_entity
  // ---------------------------------------------------------------------------

  @Test
  void should_handle_null_notes_in_entity() {
    PayrollEventEntity entity =
        PayrollEventEntity.builder()
            .id(1L)
            .userId(1L)
            .eventDate(LocalDate.of(2026, 8, 1))
            .eventType("OVERTIME")
            .quantity(new BigDecimal("2"))
            .unitRate(new BigDecimal("10000"))
            .amount(new BigDecimal("20000"))
            .notes(null)
            .createdBy("system")
            .build();

    PayrollEvent domain = mapper.toDomain(entity);

    assertNotNull(domain);
    assertNull(domain.notes());
  }

  private Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }
}
