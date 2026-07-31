package aros.services.rms.core.image.port.output;

import aros.services.rms.core.image.domain.EntityImage;
import aros.services.rms.core.image.domain.ImageEntityType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Output port for entity image persistence. */
public interface ImageRepositoryPort {
  /** Saves or updates an image. */
  EntityImage save(EntityImage image);

  /** Finds image by ID. Returns empty if not found. */
  Optional<EntityImage> findById(Long id);

  /** Finds all images for a given entity type and entity ID. */
  List<EntityImage> findByEntityTypeAndEntityId(ImageEntityType entityType, Long entityId);

  /** Finds all images for a given entity type and any of the given entity IDs. */
  List<EntityImage> findByEntityTypeAndEntityIds(
      ImageEntityType entityType, Collection<Long> entityIds);

  /** Deletes image by ID. */
  void deleteById(Long id);

  /** Deletes all images for a given entity type and entity ID. */
  void deleteByEntityTypeAndEntityId(ImageEntityType entityType, Long entityId);
}
