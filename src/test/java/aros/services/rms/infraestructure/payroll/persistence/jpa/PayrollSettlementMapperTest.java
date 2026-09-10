/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.SettlementType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PayrollSettlementMapper}. */
class PayrollSettlementMapperTest {

  private final PayrollSettlementMapper mapper = new PayrollSettlementMapper();

  // ---------------------------------------------------------------------------
  // UC-01: should_map_entity_to_domain
  // ---------------------------------------------------------------------------

  @Test
  void should_map_entity_to_domain() {
    PayrollSettlementEntity entity =
        PayrollSettlementEntity.builder()
            .id(1L)
            .payrollId(10L)
            .userId(1L)
            .settlementType("MONTHLY")
            .periodStart(LocalDate.of(2026, 8, 1))
            .periodEnd(LocalDate.of(2026, 8, 31))
            .amount(new BigDecimal("2500000"))
            .notes("August salary")
            .settledAt(Instant.parse("2026-08-31T12:00:00Z"))
            .settledBy("admin")
            .build();

    PayrollSettlement domain = mapper.toDomain(entity);

    assertNotNull(domain);
    assertEquals(1L, domain.id());
    assertEquals(10L, domain.payrollId());
    assertEquals(1L, domain.userId());
    assertEquals(SettlementType.MONTHLY, domain.settlementType());
    assertEquals(LocalDate.of(2026, 8, 1), domain.periodStart());
    assertEquals(LocalDate.of(2026, 8, 31), domain.periodEnd());
    assertEquals(0, new BigDecimal("2500000").compareTo(domain.amount()));
    assertEquals("August salary", domain.notes());
    assertEquals(Instant.parse("2026-08-31T12:00:00Z"), domain.settledAt());
    assertEquals("admin", domain.settledBy());
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_map_domain_to_entity
  // ---------------------------------------------------------------------------

  @Test
  void should_map_domain_to_entity() {
    PayrollSettlement domain =
        new PayrollSettlement(
            2L,
            15L,
            3L,
            SettlementType.BIWEEKLY,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 15),
            new BigDecimal("1250000"),
            "Half month",
            Instant.parse("2026-08-15T10:00:00Z"),
            "manager");

    PayrollSettlementEntity entity = mapper.toEntity(domain);

    assertNotNull(entity);
    assertEquals(2L, entity.getId());
    assertEquals(15L, entity.getPayrollId());
    assertEquals(3L, entity.getUserId());
    assertEquals("BIWEEKLY", entity.getSettlementType());
    assertEquals(LocalDate.of(2026, 8, 1), entity.getPeriodStart());
    assertEquals(LocalDate.of(2026, 8, 15), entity.getPeriodEnd());
    assertEquals(0, new BigDecimal("1250000").compareTo(entity.getAmount()));
    assertEquals("Half month", entity.getNotes());
    assertEquals("manager", entity.getSettledBy());
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
    PayrollSettlement original =
        new PayrollSettlement(
            20L,
            5L,
            2L,
            SettlementType.WEEKLY,
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2026, 9, 7),
            new BigDecimal("500000"),
            "Weekly pay",
            Instant.parse("2026-09-07T16:00:00Z"),
            "finance");

    PayrollSettlementEntity entity = mapper.toEntity(original);
    PayrollSettlement roundtripped = mapper.toDomain(entity);

    assertNotNull(roundtripped);
    assertEquals(original.id(), roundtripped.id());
    assertEquals(original.payrollId(), roundtripped.payrollId());
    assertEquals(original.userId(), roundtripped.userId());
    assertEquals(original.settlementType(), roundtripped.settlementType());
    assertEquals(original.periodStart(), roundtripped.periodStart());
    assertEquals(original.periodEnd(), roundtripped.periodEnd());
    assertEquals(0, original.amount().compareTo(roundtripped.amount()));
    assertEquals(original.notes(), roundtripped.notes());
    assertEquals(original.settledBy(), roundtripped.settledBy());
  }

  // ---------------------------------------------------------------------------
  // UC-06: should_handle_null_notes
  // ---------------------------------------------------------------------------

  @Test
  void should_handle_null_notes() {
    PayrollSettlementEntity entity =
        PayrollSettlementEntity.builder()
            .id(1L)
            .payrollId(10L)
            .userId(1L)
            .settlementType("DAILY")
            .periodStart(LocalDate.of(2026, 8, 1))
            .periodEnd(LocalDate.of(2026, 8, 1))
            .amount(new BigDecimal("100000"))
            .notes(null)
            .settledBy("admin")
            .build();

    PayrollSettlement domain = mapper.toDomain(entity);

    assertNotNull(domain);
    assertNull(domain.notes());
  }
}
