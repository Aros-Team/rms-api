/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.core.payroll.domain.PayrollStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * Tests for {@link PayrollMapper}.
 *
 * <p><b>Feature:</b> Persistence mapping of the payroll manual totals and legacy status.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>The manual bonus/deduction components round-trip to their own columns
 *   <li>The derived totals are mapped independently of the manual component
 *   <li>The legacy seed status {@code APPROVED} reads back as {@code ACCRUED}
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Uses the generated MapStruct mapper directly with an explicit currency
 *   <li>Asserts every monetary field of the resulting entity/domain object
 * </ul>
 */
class PayrollMapperTest {

  private static final Currency COP = Currency.getInstance("COP");
  private final PayrollMapper mapper = Mappers.getMapper(PayrollMapper.class);

  // ---------------------------------------------------------------------------
  // UC-01: should_map_manual_and_derived_totals_to_entity
  // ---------------------------------------------------------------------------

  @Test
  void should_map_manual_and_derived_totals_to_entity() {
    Payroll domain =
        new Payroll(
            1L,
            7L,
            YearMonth.of(2026, 8),
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 31),
            money("1500000"),
            money("100000"),
            money("20000"),
            money("158593.75"),
            money("95000"),
            money("1563593.75"),
            money("800000"),
            new BigDecimal("192"),
            PayrollStatus.PENDING,
            "notes",
            10L,
            Instant.parse("2026-08-01T00:00:00Z"),
            Instant.parse("2026-08-31T00:00:00Z"));

    PayrollEntity entity = mapper.toEntity(domain);

    assertEquals(1L, entity.getId());
    assertEquals(7L, entity.getUserId());
    assertEquals(2026, entity.getPeriodYear());
    assertEquals(8, entity.getPeriodMonth());
    assertEquals(0, new BigDecimal("1500000").compareTo(entity.getBaseSalary()));
    assertEquals(0, new BigDecimal("100000").compareTo(entity.getManualBonuses()));
    assertEquals(0, new BigDecimal("20000").compareTo(entity.getManualDeductions()));
    assertEquals(0, new BigDecimal("158593.75").compareTo(entity.getBonuses()));
    assertEquals(0, new BigDecimal("95000").compareTo(entity.getDeductions()));
    assertEquals(0, new BigDecimal("1563593.75").compareTo(entity.getNetAmount()));
    assertEquals(0, new BigDecimal("800000").compareTo(entity.getPaidAmount()));
    assertEquals("PENDING", entity.getStatus());
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_map_entity_to_domain_with_manual_totals
  // ---------------------------------------------------------------------------

  @Test
  void should_map_entity_to_domain_with_manual_totals() {
    PayrollEntity entity = new PayrollEntity();
    entity.setId(2L);
    entity.setUserId(8L);
    entity.setPeriodYear(2026);
    entity.setPeriodMonth(9);
    entity.setPeriodStart(LocalDate.of(2026, 9, 1));
    entity.setPeriodEnd(LocalDate.of(2026, 9, 30));
    entity.setBaseSalary(new BigDecimal("2000000"));
    entity.setManualBonuses(new BigDecimal("50000"));
    entity.setManualDeductions(new BigDecimal("10000"));
    entity.setBonuses(new BigDecimal("150000"));
    entity.setDeductions(new BigDecimal("60000"));
    entity.setNetAmount(new BigDecimal("2090000"));
    entity.setPaidAmount(new BigDecimal("1000000"));
    entity.setHoursWorked(new BigDecimal("160"));
    entity.setStatus("APPROVED");
    entity.setNotes("September");
    entity.setRegisteredBy(10L);
    entity.setCreatedAt(Instant.parse("2026-09-01T00:00:00Z"));
    entity.setUpdatedAt(Instant.parse("2026-09-30T00:00:00Z"));

    Payroll domain = mapper.toDomain(entity, COP);

    assertEquals(PayrollStatus.ACCRUED, domain.status());
    assertEquals(0, new BigDecimal("50000").compareTo(domain.manualBonuses().amount()));
    assertEquals(0, new BigDecimal("10000").compareTo(domain.manualDeductions().amount()));
    assertEquals(0, new BigDecimal("150000").compareTo(domain.bonuses().amount()));
    assertEquals(0, new BigDecimal("60000").compareTo(domain.deductions().amount()));
    assertEquals(COP, domain.netAmount().currency());
  }

  private Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }
}
