/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.core.inventory.domain.SupplyVariant;
import aros.services.rms.infraestructure.inventory.persistence.SupplyVariantEntity;
import org.springframework.stereotype.Component;

/** Mapper between SupplyVariant domain model and SupplyVariantEntity JPA entity. */
@Component
public class SupplyVariantMapper {

  /** Converts a SupplyVariantEntity JPA entity to a domain model. */
  public SupplyVariant toDomain(SupplyVariantEntity entity) {
    if (entity == null) return null;
    return SupplyVariant.builder()
        .id(entity.getId())
        .supplyId(entity.getSupply() != null ? entity.getSupply().getId() : null)
        .unitId(entity.getUnit() != null ? entity.getUnit().getId() : null)
        .quantity(entity.getQuantity())
        .build();
  }

  /** Converts a SupplyVariant domain model to a JPA entity. */
  public SupplyVariantEntity toEntity(SupplyVariant domain) {
    if (domain == null) return null;
    return SupplyVariantEntity.builder().id(domain.getId()).quantity(domain.getQuantity()).build();
  }
}
