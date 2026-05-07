package aros.services.rms.core.image.port.input;

import aros.services.rms.core.image.domain.ImageEntityType;

/** Input port for deleting entity images. */
public interface DeleteImageUseCase {
  /** Deletes a single image by ID and removes all its versions from storage. */
  void delete(Long imageId);

  /** Deletes all images for a given entity type and entity ID. */
  void deleteByEntity(ImageEntityType entityType, Long entityId);
}
