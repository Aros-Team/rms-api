package aros.services.rms.core.image.port.input;

import aros.services.rms.core.image.domain.ImageEntityType;
import aros.services.rms.core.image.domain.ImageWithUrls;
import java.util.List;

/** Input port for retrieving entity images. */
public interface GetImagesUseCase {
  /** Gets all images for a given entity type and entity ID with signed URLs. */
  List<ImageWithUrls> getByEntity(ImageEntityType entityType, Long entityId);

  /** Gets a single image by ID with signed URLs. */
  ImageWithUrls getById(Long imageId);
}
