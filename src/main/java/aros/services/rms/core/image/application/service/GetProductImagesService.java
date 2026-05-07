package aros.services.rms.core.image.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.image.application.exception.ProductImageNotFoundException;
import aros.services.rms.core.image.domain.ProductImage;
import aros.services.rms.core.image.domain.ProductImageWithUrls;
import aros.services.rms.core.image.port.input.GetProductImagesUseCase;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/** Service implementation for retrieving product images with signed URLs. */
public class GetProductImagesService implements GetProductImagesUseCase {

  private static final Duration SIGNED_URL_EXPIRATION = Duration.ofMinutes(60);

  private final ImageRepositoryPort imageRepositoryPort;
  private final StoragePort storagePort;
  private final Logger logger;

  /** Creates service with required ports. */
  public GetProductImagesService(
      ImageRepositoryPort imageRepositoryPort, StoragePort storagePort, Logger logger) {
    this.imageRepositoryPort = imageRepositoryPort;
    this.storagePort = storagePort;
    this.logger = logger;
  }

  /** Gets all images for a product with signed URLs. */
  @Override
  public List<ProductImageWithUrls> getByProductId(Long productId) {
    List<ProductImage> images = imageRepositoryPort.findByProductId(productId);
    return images.stream().map(this::toWithUrls).collect(Collectors.toList());
  }

  /** Gets a single image by ID with signed URLs. */
  @Override
  public ProductImageWithUrls getById(Long imageId) {
    ProductImage image =
        imageRepositoryPort
            .findById(imageId)
            .orElseThrow(() -> new ProductImageNotFoundException(imageId));
    return toWithUrls(image);
  }

  private ProductImageWithUrls toWithUrls(ProductImage image) {
    String baseKey = image.getStorageKey();
    return ProductImageWithUrls.builder()
        .image(image)
        .mobileUrl(storagePort.generateSignedUrl(baseKey + "/mobile.webp", SIGNED_URL_EXPIRATION))
        .tabletUrl(storagePort.generateSignedUrl(baseKey + "/tablet.webp", SIGNED_URL_EXPIRATION))
        .desktopUrl(storagePort.generateSignedUrl(baseKey + "/desktop.webp", SIGNED_URL_EXPIRATION))
        .build();
  }
}
