/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.core.inventory.domain.StorageLocation;
import aros.services.rms.infraestructure.inventory.persistence.StorageLocationEntity;
import org.springframework.stereotype.Component;

/** Mapper between StorageLocation domain model and StorageLocationEntity JPA entity. */
@Component
public class StorageLocationMapper {

  /** Converts a StorageLocationEntity JPA entity to a domain model. */
  public StorageLocation toDomain(StorageLocationEntity entity) {
    if (entity == null) return null;
    return StorageLocation.builder().id(entity.getId()).name(entity.getName()).build();
  }

  /** Converts a StorageLocation domain model to a JPA entity. */
  public StorageLocationEntity toEntity(StorageLocation domain) {
    if (domain == null) return null;
    return StorageLocationEntity.builder().id(domain.getId()).name(domain.getName()).build();
  }
}
