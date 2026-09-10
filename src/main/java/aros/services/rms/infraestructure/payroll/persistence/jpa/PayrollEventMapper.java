/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import aros.services.rms.core.payroll.domain.PayrollEvent;
import aros.services.rms.core.payroll.domain.PayrollEventType;
import org.springframework.stereotype.Component;

/** Mapper for PayrollEvent persistence operations. */
@Component
public class PayrollEventMapper {

  /**
   * Converts a PayrollEventEntity to a PayrollEvent domain object.
   *
   * @param entity the entity to convert
   * @return the domain object, or null if entity is null
   */
  public PayrollEvent toDomain(PayrollEventEntity entity) {
    if (entity == null) {
      return null;
    }
    return new PayrollEvent(
        entity.getId(),
        entity.getUserId(),
        entity.getEventDate(),
        PayrollEventType.valueOf(entity.getEventType()),
        entity.getQuantity(),
        entity.getUnitRate(),
        entity.getAmount(),
        entity.getNotes(),
        entity.getCreatedAt(),
        entity.getCreatedBy());
  }

  /**
   * Converts a PayrollEvent domain object to a PayrollEventEntity.
   *
   * @param domain the domain object to convert
   * @return the entity, or null if domain is null
   */
  public PayrollEventEntity toEntity(PayrollEvent domain) {
    if (domain == null) {
      return null;
    }
    return PayrollEventEntity.builder()
        .id(domain.id())
        .userId(domain.userId())
        .eventDate(domain.eventDate())
        .eventType(domain.eventType().name())
        .quantity(domain.quantity())
        .unitRate(domain.unitRate())
        .amount(domain.amount())
        .notes(domain.notes())
        .createdBy(domain.createdBy())
        .build();
  }
}
