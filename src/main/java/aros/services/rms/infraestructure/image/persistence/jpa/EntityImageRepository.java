/* (C) 2026 */

package aros.services.rms.infraestructure.image.persistence.jpa;

import aros.services.rms.core.image.domain.ImageEntityType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for EntityImage entities. */
@Repository
public interface EntityImageRepository extends JpaRepository<EntityImageEntity, Long> {
  /** Finds all images for a given entity type and entity ID. */
  List<EntityImageEntity> findByEntityTypeAndEntityId(ImageEntityType entityType, Long entityId);

  /** Deletes all images for a given entity type and entity ID. */
  void deleteByEntityTypeAndEntityId(ImageEntityType entityType, Long entityId);
}
