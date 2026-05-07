/* (C) 2026 */

package aros.services.rms.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.image.application.exception.InvalidImageException;
import aros.services.rms.core.image.application.service.DeleteProductImageService;
import aros.services.rms.core.image.application.service.GetProductImagesService;
import aros.services.rms.core.image.application.service.UploadProductImageService;
import aros.services.rms.core.image.domain.ImageSize;
import aros.services.rms.core.image.domain.ProductImage;
import aros.services.rms.core.image.domain.ProductImageWithUrls;
import aros.services.rms.core.image.port.output.ImageProcessingPort;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Integration-style test covering the full upload → get → delete cycle. */
@ExtendWith(MockitoExtension.class)
class ImageIntegrationTest {

  @Mock private ImageProcessingPort imageProcessingPort;
  @Mock private StoragePort storagePort;
  @Mock private ImageRepositoryPort imageRepositoryPort;
  @Mock private Logger logger;

  private UploadProductImageService uploadService;
  private GetProductImagesService getService;
  private DeleteProductImageService deleteService;

  @BeforeEach
  void setUp() {
    uploadService =
        new UploadProductImageService(
            imageProcessingPort, storagePort, imageRepositoryPort, logger);
    getService = new GetProductImagesService(imageRepositoryPort, storagePort, logger);
    deleteService = new DeleteProductImageService(imageRepositoryPort, storagePort, logger);
  }

  @Test
  void shouldUploadProcessAndReturnImage() {
    byte[] imageData = new byte[1024];
    ProductImage savedImage =
        ProductImage.builder()
            .id(1L)
            .productId(42L)
            .originalFilename("test.jpg")
            .contentType("image/jpeg")
            .originalSizeBytes(1024L)
            .storageKey("products/42/abc-123")
            .build();

    when(imageProcessingPort.processAllVersions(any(byte[].class), eq("image/jpeg")))
        .thenReturn(
            Map.of(
                ImageSize.MOBILE, new byte[100],
                ImageSize.TABLET, new byte[200],
                ImageSize.DESKTOP, new byte[300]));
    when(storagePort.store(anyString(), any(ByteArrayInputStream.class), eq("image/webp")))
        .thenReturn("http://localhost/images/version.webp");
    when(imageRepositoryPort.save(any(ProductImage.class))).thenReturn(savedImage);

    ProductImage result = uploadService.upload(42L, "test.jpg", "image/jpeg", imageData);

    assertNotNull(result);
    assertEquals(42L, result.getProductId());
    assertEquals("test.jpg", result.getOriginalFilename());
    verify(imageProcessingPort).validate(imageData, "image/jpeg");
    verify(storagePort, times(3))
        .store(anyString(), any(ByteArrayInputStream.class), eq("image/webp"));
    verify(imageRepositoryPort).save(any(ProductImage.class));
  }

  @Test
  void shouldGetImagesWithSignedUrls() {
    ProductImage image =
        ProductImage.builder()
            .id(1L)
            .productId(42L)
            .originalFilename("test.jpg")
            .contentType("image/jpeg")
            .originalSizeBytes(1024L)
            .storageKey("products/42/abc-123")
            .build();

    when(imageRepositoryPort.findByProductId(42L)).thenReturn(List.of(image));
    when(storagePort.generateSignedUrl(eq("products/42/abc-123/mobile.webp"), any(Duration.class)))
        .thenReturn("http://localhost/mobile.webp");
    when(storagePort.generateSignedUrl(eq("products/42/abc-123/tablet.webp"), any(Duration.class)))
        .thenReturn("http://localhost/tablet.webp");
    when(storagePort.generateSignedUrl(eq("products/42/abc-123/desktop.webp"), any(Duration.class)))
        .thenReturn("http://localhost/desktop.webp");

    List<ProductImageWithUrls> results = getService.getByProductId(42L);

    assertNotNull(results);
    assertEquals(1, results.size());
    ProductImageWithUrls withUrls = results.get(0);
    assertNotNull(withUrls.getMobileUrl());
    assertNotNull(withUrls.getTabletUrl());
    assertNotNull(withUrls.getDesktopUrl());
    assertTrue(withUrls.getMobileUrl().contains("mobile.webp"));
  }

  @Test
  void shouldDeleteImageFromStorageAndDatabase() {
    ProductImage image =
        ProductImage.builder().id(1L).productId(42L).storageKey("products/42/abc-123").build();

    when(imageRepositoryPort.findById(1L)).thenReturn(Optional.of(image));

    deleteService.delete(1L);

    verify(storagePort).delete("products/42/abc-123/mobile.webp");
    verify(storagePort).delete("products/42/abc-123/tablet.webp");
    verify(storagePort).delete("products/42/abc-123/desktop.webp");
    verify(imageRepositoryPort).deleteById(1L);
  }

  @Test
  void shouldRejectInvalidFileType() {
    byte[] imageData = new byte[100];
    doThrow(new InvalidImageException("Unsupported image format: text/plain"))
        .when(imageProcessingPort)
        .validate(imageData, "text/plain");

    assertThrows(
        InvalidImageException.class,
        () -> uploadService.upload(1L, "test.txt", "text/plain", imageData));
  }

  @Test
  void shouldRejectOversizedFile() {
    byte[] imageData = new byte[6 * 1024 * 1024];
    doThrow(new InvalidImageException("Image size exceeds maximum allowed size of 5MB"))
        .when(imageProcessingPort)
        .validate(imageData, "image/jpeg");

    assertThrows(
        InvalidImageException.class,
        () -> uploadService.upload(1L, "big.jpg", "image/jpeg", imageData));
  }
}
