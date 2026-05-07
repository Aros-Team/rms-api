package aros.services.rms.core.image.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.image.application.exception.ImageNotFoundException;
import aros.services.rms.core.image.domain.EntityImage;
import aros.services.rms.core.image.domain.ImageEntityType;
import aros.services.rms.core.image.domain.ImageWithUrls;
import aros.services.rms.core.image.port.input.GetImagesUseCase;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/** Service implementation for retrieving entity images with signed URLs. */
public class GetImagesService implements GetImagesUseCase {

  private static final Duration SIGNED_URL_EXPIRATION = Duration.ofMinutes(60);

  private final ImageRepositoryPort imageRepositoryPort;
  private final StoragePort storagePort;
  private final Logger logger;

  /** Creates service with required ports. */
  public GetImagesService(
      ImageRepositoryPort imageRepositoryPort, StoragePort storagePort, Logger logger) {
    this.imageRepositoryPort = imageRepositoryPort;
    this.storagePort = storagePort;
    this.logger = logger;
  }

  @Override
  public List<ImageWithUrls> getByEntity(ImageEntityType entityType, Long entityId) {
    List<EntityImage> images =
        imageRepositoryPort.findByEntityTypeAndEntityId(entityType, entityId);
    return images.stream().map(this::toWithUrls).collect(Collectors.toList());
  }

  @Override
  public ImageWithUrls getById(Long imageId) {
    EntityImage image =
        imageRepositoryPort
            .findById(imageId)
            .orElseThrow(() -> new ImageNotFoundException(imageId));
    return toWithUrls(image);
  }

  private ImageWithUrls toWithUrls(EntityImage image) {
    String baseKey = image.getStorageKey();
    return ImageWithUrls.builder()
        .image(image)
        .mobileUrl(storagePort.generateSignedUrl(baseKey + "/mobile.webp", SIGNED_URL_EXPIRATION))
        .tabletUrl(storagePort.generateSignedUrl(baseKey + "/tablet.webp", SIGNED_URL_EXPIRATION))
        .desktopUrl(storagePort.generateSignedUrl(baseKey + "/desktop.webp", SIGNED_URL_EXPIRATION))
        .build();
  }
}
