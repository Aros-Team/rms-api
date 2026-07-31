/* (C) 2026 */

package aros.services.rms.infraestructure.image.persistence.jpa;

import aros.services.rms.core.image.domain.EntityImage;
import aros.services.rms.core.image.domain.ImageEntityType;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Persistence adapter that implements ImageRepositoryPort using JPA. */
@Component
@RequiredArgsConstructor
public class EntityImagePersistenceAdapter implements ImageRepositoryPort {

  private final EntityImageRepository repository;
  private final ImageMapper mapper;

  @Override
  public EntityImage save(EntityImage image) {
    EntityImageEntity entity = mapper.toEntity(image);
    EntityImageEntity savedEntity = repository.save(entity);
    return mapper.toDomain(savedEntity);
  }

  @Override
  public Optional<EntityImage> findById(Long id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<EntityImage> findByEntityTypeAndEntityId(ImageEntityType entityType, Long entityId) {
    return repository.findByEntityTypeAndEntityId(entityType, entityId).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<EntityImage> findByEntityTypeAndEntityIds(
      ImageEntityType entityType, Collection<Long> entityIds) {
    if (entityIds == null || entityIds.isEmpty()) {
      return List.of();
    }
    return repository.findByEntityTypeAndEntityIdIn(entityType, entityIds).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteById(Long id) {
    repository.deleteById(id);
  }

  @Override
  public void deleteByEntityTypeAndEntityId(ImageEntityType entityType, Long entityId) {
    repository.deleteByEntityTypeAndEntityId(entityType, entityId);
  }
}
