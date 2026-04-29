/* (C) 2026 */

package aros.services.rms.infraestructure.purchase.persistence.jpa;

import aros.services.rms.core.purchase.domain.Supplier;
import aros.services.rms.infraestructure.purchase.persistence.SupplierEntity;
import org.springframework.stereotype.Component;

/** Mapper between Supplier domain model and SupplierEntity JPA entity. */
@Component
public class SupplierMapper {

  /**
   * Converts an entity to domain.
   *
   * @param entity the entity
   * @return the domain
   */
  public Supplier toDomain(SupplierEntity entity) {
    if (entity == null) {
      return null;
    }
    return Supplier.builder()
        .id(entity.getId())
        .name(entity.getName())
        .contact(entity.getContact())
        .active(entity.isActive())
        .build();
  }

  /**
   * Converts a domain to entity.
   *
   * @param domain the domain
   * @return the entity
   */
  public SupplierEntity toEntity(Supplier domain) {
    if (domain == null) {
      return null;
    }
    return SupplierEntity.builder()
        .id(domain.getId())
        .name(domain.getName())
        .contact(domain.getContact())
        .active(domain.isActive())
        .build();
  }
}
