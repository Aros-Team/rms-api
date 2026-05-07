/* (C) 2026 */

package aros.services.rms.infraestructure.image.persistence.jpa;

import aros.services.rms.core.image.domain.EntityImage;
import org.springframework.stereotype.Component;

/** Mapper between EntityImage domain model and EntityImage JPA entity. */
@Component
public class ImageMapper {

  /** Converts a domain EntityImage to a JPA entity. */
  public EntityImageEntity toEntity(EntityImage domain) {
    if (domain == null) {
      return null;
    }

    return EntityImageEntity.builder()
        .id(domain.getId())
        .entityType(domain.getEntityType())
        .entityId(domain.getEntityId())
        .originalFilename(domain.getOriginalFilename())
        .contentType(domain.getContentType())
        .originalSizeBytes(domain.getOriginalSizeBytes())
        .storageKey(domain.getStorageKey())
        .createdAt(domain.getCreatedAt())
        .build();
  }

  /** Converts a JPA entity to a domain model. */
  public EntityImage toDomain(EntityImageEntity entity) {
    if (entity == null) {
      return null;
    }

    return EntityImage.builder()
        .id(entity.getId())
        .entityType(entity.getEntityType())
        .entityId(entity.getEntityId())
        .originalFilename(entity.getOriginalFilename())
        .contentType(entity.getContentType())
        .originalSizeBytes(entity.getOriginalSizeBytes())
        .storageKey(entity.getStorageKey())
        .createdAt(entity.getCreatedAt())
        .build();
  }
}
