/* (C) 2026 */

package aros.services.rms.core.image.port.input;

import aros.services.rms.core.image.domain.EntityImage;
import aros.services.rms.core.image.domain.ImageEntityType;

/** Input port for uploading entity images. */
public interface UploadImageUseCase {
  /**
   * Uploads an image for any entity type, processing it into multiple size versions.
   *
   * @param entityType the type of entity (PRODUCT, USER, etc.)
   * @param entityId the identifier of the entity
   * @param originalFilename the original file name
   * @param contentType the MIME type of the image
   * @param imageData the raw image bytes
   * @return the saved image
   */
  EntityImage upload(
      ImageEntityType entityType,
      Long entityId,
      String originalFilename,
      String contentType,
      byte[] imageData);
}
