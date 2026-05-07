package aros.services.rms.core.image.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.image.application.exception.ProductImageNotFoundException;
import aros.services.rms.core.image.domain.ImageSize;
import aros.services.rms.core.image.domain.ProductImage;
import aros.services.rms.core.image.port.input.DeleteProductImageUseCase;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;

/** Service implementation for deleting product images. */
public class DeleteProductImageService implements DeleteProductImageUseCase {

  private final ImageRepositoryPort imageRepositoryPort;
  private final StoragePort storagePort;
  private final Logger logger;

  /** Creates service with required ports. */
  public DeleteProductImageService(
      ImageRepositoryPort imageRepositoryPort, StoragePort storagePort, Logger logger) {
    this.imageRepositoryPort = imageRepositoryPort;
    this.storagePort = storagePort;
    this.logger = logger;
  }

  /** Deletes image and all its size versions from storage and database. */
  @Override
  public void delete(Long imageId) {
    ProductImage image =
        imageRepositoryPort
            .findById(imageId)
            .orElseThrow(() -> new ProductImageNotFoundException(imageId));

    String baseKey = image.getStorageKey();

    for (ImageSize size : ImageSize.values()) {
      String versionKey = baseKey + "/" + size.name().toLowerCase() + ".webp";
      try {
        storagePort.delete(versionKey);
      } catch (Exception e) {
        logger.error("Failed to delete image version from storage: " + versionKey, e);
      }
    }

    imageRepositoryPort.deleteById(imageId);
    logger.info("Deleted image {} for product {}", imageId, image.getProductId());
  }
}
