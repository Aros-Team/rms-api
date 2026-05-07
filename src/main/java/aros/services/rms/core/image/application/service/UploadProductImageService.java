/* (C) 2026 */

package aros.services.rms.core.image.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.image.application.exception.ImageUploadException;
import aros.services.rms.core.image.application.exception.InvalidImageException;
import aros.services.rms.core.image.domain.ImageSize;
import aros.services.rms.core.image.domain.ProductImage;
import aros.services.rms.core.image.port.input.UploadProductImageUseCase;
import aros.services.rms.core.image.port.output.ImageProcessingPort;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.UUID;

/** Service implementation for uploading product images with multi-size processing. */
public class UploadProductImageService implements UploadProductImageUseCase {

  private final ImageProcessingPort imageProcessingPort;
  private final StoragePort storagePort;
  private final ImageRepositoryPort imageRepositoryPort;
  private final Logger logger;

  /**
   * Creates an upload product image service.
   *
   * @param imageProcessingPort port for image validation and processing
   * @param storagePort port for storing image files
   * @param imageRepositoryPort port for persisting image metadata
   * @param logger the logger
   */
  public UploadProductImageService(
      ImageProcessingPort imageProcessingPort,
      StoragePort storagePort,
      ImageRepositoryPort imageRepositoryPort,
      Logger logger) {
    this.imageProcessingPort = imageProcessingPort;
    this.storagePort = storagePort;
    this.imageRepositoryPort = imageRepositoryPort;
    this.logger = logger;
  }

  /** Uploads image, processes into multiple sizes, and stores. */
  @Override
  public ProductImage upload(
      Long productId, String originalFilename, String contentType, byte[] imageData) {
    try {
      imageProcessingPort.validate(imageData, contentType);

      String storageKey = generateStorageKey(productId);
      Map<ImageSize, byte[]> versions =
          imageProcessingPort.processAllVersions(imageData, contentType);

      for (ImageSize size : ImageSize.values()) {
        byte[] versionData = versions.get(size);
        String versionKey = storageKey + "/" + size.name().toLowerCase() + ".webp";
        storagePort.store(versionKey, new ByteArrayInputStream(versionData), "image/webp");
      }

      ProductImage productImage =
          ProductImage.builder()
              .productId(productId)
              .originalFilename(originalFilename)
              .contentType(contentType)
              .originalSizeBytes(imageData.length)
              .storageKey(storageKey)
              .build();

      ProductImage saved = imageRepositoryPort.save(productImage);
      logger.info("Uploaded image for product {}: {}", productId, saved.getId());
      return saved;
    } catch (InvalidImageException e) {
      throw e;
    } catch (Exception e) {
      logger.error("Failed to upload image for product " + productId, e);
      throw new ImageUploadException("Failed to upload image: " + e.getMessage());
    }
  }

  private String generateStorageKey(Long productId) {
    return "products/" + productId + "/" + UUID.randomUUID();
  }
}
