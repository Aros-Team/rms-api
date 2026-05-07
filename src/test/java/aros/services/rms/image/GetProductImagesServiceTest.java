package aros.services.rms.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.image.application.exception.ProductImageNotFoundException;
import aros.services.rms.core.image.application.service.GetProductImagesService;
import aros.services.rms.core.image.domain.ProductImage;
import aros.services.rms.core.image.domain.ProductImageWithUrls;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetProductImagesServiceTest {

  @Mock private ImageRepositoryPort imageRepositoryPort;

  @Mock private StoragePort storagePort;

  @Mock private Logger logger;

  private GetProductImagesService getService;

  @BeforeEach
  void setUp() {
    getService = new GetProductImagesService(imageRepositoryPort, storagePort, logger);
  }

  @Test
  void shouldReturnImagesWithSignedUrls() {
    ProductImage image =
        ProductImage.builder()
            .id(1L)
            .productId(42L)
            .originalFilename("test.jpg")
            .contentType("image/jpeg")
            .originalSizeBytes(1024L)
            .storageKey("products/42/uuid")
            .build();

    when(imageRepositoryPort.findByProductId(42L)).thenReturn(List.of(image));
    when(storagePort.generateSignedUrl(anyString(), any(Duration.class)))
        .thenReturn("http://signed-url.com/image.webp");

    List<ProductImageWithUrls> result = getService.getByProductId(42L);

    assertEquals(1, result.size());
    ProductImageWithUrls withUrls = result.get(0);
    assertNotNull(withUrls.getMobileUrl());
    assertNotNull(withUrls.getTabletUrl());
    assertNotNull(withUrls.getDesktopUrl());
    verify(storagePort, times(3)).generateSignedUrl(anyString(), eq(Duration.ofMinutes(60)));
  }

  @Test
  void shouldReturnEmptyListWhenProductHasNoImages() {
    when(imageRepositoryPort.findByProductId(99L)).thenReturn(List.of());

    List<ProductImageWithUrls> result = getService.getByProductId(99L);

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldThrowWhenImageNotFound() {
    when(imageRepositoryPort.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ProductImageNotFoundException.class, () -> getService.getById(999L));
  }

  @Test
  void shouldGenerateSignedUrlWith60MinExpiration() {
    ProductImage image =
        ProductImage.builder().id(1L).productId(42L).storageKey("products/42/abc").build();

    when(imageRepositoryPort.findById(1L)).thenReturn(Optional.of(image));
    when(storagePort.generateSignedUrl(anyString(), any(Duration.class))).thenReturn("url");

    getService.getById(1L);

    verify(storagePort).generateSignedUrl(contains("mobile.webp"), eq(Duration.ofMinutes(60)));
    verify(storagePort).generateSignedUrl(contains("tablet.webp"), eq(Duration.ofMinutes(60)));
    verify(storagePort).generateSignedUrl(contains("desktop.webp"), eq(Duration.ofMinutes(60)));
  }
}
