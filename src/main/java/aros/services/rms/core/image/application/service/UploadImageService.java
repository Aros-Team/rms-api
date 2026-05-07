/* (C) 2026 */

package aros.services.rms.core.image.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.image.application.exception.ImageUploadException;
import aros.services.rms.core.image.application.exception.InvalidImageException;
import aros.services.rms.core.image.domain.EntityImage;
import aros.services.rms.core.image.domain.ImageEntityType;
import aros.services.rms.core.image.domain.ImageSize;
import aros.services.rms.core.image.port.input.UploadImageUseCase;
import aros.services.rms.core.image.port.output.ImageProcessingPort;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.UUID;

/** Service implementation for uploading entity images with multi-size processing. */
public class UploadImageService implements UploadImageUseCase {

  private final ImageProcessingPort imageProcessingPort;
  private final StoragePort storagePort;
  private final ImageRepositoryPort imageRepositoryPort;
  private final Logger logger;

  /** Creates an upload image service. */
  public UploadImageService(
      ImageProcessingPort imageProcessingPort,
      StoragePort storagePort,
      ImageRepositoryPort imageRepositoryPort,
      Logger logger) {
    this.imageProcessingPort = imageProcessingPort;
    this.storagePort = storagePort;
    this.imageRepositoryPort = imageRepositoryPort;
    this.logger = logger;
  }

  @Override
  public EntityImage upload(
      ImageEntityType entityType,
      Long entityId,
      String originalFilename,
      String contentType,
      byte[] imageData) {
    try {
      imageProcessingPort.validate(imageData, contentType);

      String storageKey = generateStorageKey(entityType, entityId);
      Map<ImageSize, byte[]> versions =
          imageProcessingPort.processAllVersions(imageData, contentType);

      for (ImageSize size : ImageSize.values()) {
        byte[] versionData = versions.get(size);
        String versionKey = storageKey + "/" + size.name().toLowerCase() + ".webp";
        storagePort.store(versionKey, new ByteArrayInputStream(versionData), "image/webp");
      }

      EntityImage image =
          EntityImage.builder()
              .entityType(entityType)
              .entityId(entityId)
              .originalFilename(originalFilename)
              .contentType(contentType)
              .originalSizeBytes(imageData.length)
              .storageKey(storageKey)
              .build();

      EntityImage saved = imageRepositoryPort.save(image);
      logger.info("Uploaded image for {} {}: {}", entityType, entityId, saved.getId());
      return saved;
    } catch (InvalidImageException e) {
      throw e;
    } catch (Exception e) {
      logger.error("Failed to upload image for " + entityType + " " + entityId, e);
      throw new ImageUploadException("Failed to upload image: " + e.getMessage());
    }
  }

  private String generateStorageKey(ImageEntityType entityType, Long entityId) {
    return entityType.name().toLowerCase() + "s/" + entityId + "/" + UUID.randomUUID();
  }
}
