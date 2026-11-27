package aros.services.rms.core.image.port.input;

import aros.services.rms.core.image.domain.ImageEntityType;
import aros.services.rms.core.image.domain.ImageWithUrls;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Input port for retrieving entity images. */
public interface GetImagesUseCase {
  /** Gets all images for a given entity type and entity ID with signed URLs. */
  List<ImageWithUrls> getByEntity(ImageEntityType entityType, Long entityId);

  /** Gets a single image by ID with signed URLs. */
  ImageWithUrls getById(Long imageId);

  /**
   * Gets the first image of each entity, keyed by entity ID.
   *
   * @param entityType entity type that owns the images
   * @param entityIds entity IDs to resolve; empty when no entity has an image
   * @return map of entity ID to its first image with signed URLs
   */
  Map<Long, ImageWithUrls> getFirstByEntities(
      ImageEntityType entityType, Collection<Long> entityIds);
}
