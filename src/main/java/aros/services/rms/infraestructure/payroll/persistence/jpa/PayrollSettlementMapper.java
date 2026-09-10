/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import aros.services.rms.core.payroll.domain.PayrollSettlement;
import aros.services.rms.core.payroll.domain.SettlementType;
import org.springframework.stereotype.Component;

/** Mapper for PayrollSettlement persistence operations. */
@Component
public class PayrollSettlementMapper {

  /**
   * Converts a PayrollSettlementEntity to a PayrollSettlement domain object.
   *
   * @param entity the entity to convert
   * @return the domain object, or null if entity is null
   */
  public PayrollSettlement toDomain(PayrollSettlementEntity entity) {
    if (entity == null) {
      return null;
    }
    return new PayrollSettlement(
        entity.getId(),
        entity.getPayrollId(),
        entity.getUserId(),
        SettlementType.valueOf(entity.getSettlementType()),
        entity.getPeriodStart(),
        entity.getPeriodEnd(),
        entity.getAmount(),
        entity.getNotes(),
        entity.getSettledAt(),
        entity.getSettledBy());
  }

  /**
   * Converts a PayrollSettlement domain object to a PayrollSettlementEntity.
   *
   * @param domain the domain object to convert
   * @return the entity, or null if domain is null
   */
  public PayrollSettlementEntity toEntity(PayrollSettlement domain) {
    if (domain == null) {
      return null;
    }
    return PayrollSettlementEntity.builder()
        .id(domain.id())
        .payrollId(domain.payrollId())
        .userId(domain.userId())
        .settlementType(domain.settlementType().name())
        .periodStart(domain.periodStart())
        .periodEnd(domain.periodEnd())
        .amount(domain.amount())
        .notes(domain.notes())
        .settledBy(domain.settledBy())
        .build();
  }
}
