package aros.services.rms.core.image.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.image.application.exception.ImageNotFoundException;
import aros.services.rms.core.image.domain.EntityImage;
import aros.services.rms.core.image.domain.ImageEntityType;
import aros.services.rms.core.image.port.input.DeleteImageUseCase;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import java.util.List;

/** Service implementation for deleting entity images. */
public class DeleteImageService implements DeleteImageUseCase {

  private final ImageRepositoryPort imageRepositoryPort;
  private final StoragePort storagePort;
  private final Logger logger;

  /** Creates service with required ports. */
  public DeleteImageService(
      ImageRepositoryPort imageRepositoryPort, StoragePort storagePort, Logger logger) {
    this.imageRepositoryPort = imageRepositoryPort;
    this.storagePort = storagePort;
    this.logger = logger;
  }

  @Override
  public void delete(Long imageId) {
    EntityImage image =
        imageRepositoryPort
            .findById(imageId)
            .orElseThrow(() -> new ImageNotFoundException(imageId));

    deleteAllVersions(image);
    imageRepositoryPort.deleteById(imageId);
    logger.info("Deleted image {} from storage", imageId);
  }

  @Override
  public void deleteByEntity(ImageEntityType entityType, Long entityId) {
    List<EntityImage> images =
        imageRepositoryPort.findByEntityTypeAndEntityId(entityType, entityId);

    for (EntityImage image : images) {
      deleteAllVersions(image);
    }
    imageRepositoryPort.deleteByEntityTypeAndEntityId(entityType, entityId);
    logger.info("Deleted all images for {} {}", entityType, entityId);
  }

  private void deleteAllVersions(EntityImage image) {
    String baseKey = image.getStorageKey();
    for (var size : aros.services.rms.core.image.domain.ImageSize.values()) {
      String versionKey = baseKey + "/" + size.name().toLowerCase() + ".webp";
      storagePort.delete(versionKey);
    }
  }
}
