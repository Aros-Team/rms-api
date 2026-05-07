/* (C) 2026 */

package aros.services.rms.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.image.application.exception.InvalidImageException;
import aros.services.rms.core.image.application.service.UploadProductImageService;
import aros.services.rms.core.image.domain.ImageSize;
import aros.services.rms.core.image.domain.ProductImage;
import aros.services.rms.core.image.port.output.ImageProcessingPort;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import java.io.ByteArrayInputStream;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UploadProductImageServiceTest {

  @Mock private ImageProcessingPort imageProcessingPort;

  @Mock private StoragePort storagePort;

  @Mock private ImageRepositoryPort imageRepositoryPort;

  @Mock private Logger logger;

  private UploadProductImageService uploadService;

  @BeforeEach
  void setUp() {
    uploadService =
        new UploadProductImageService(
            imageProcessingPort, storagePort, imageRepositoryPort, logger);
  }

  @Test
  void shouldUploadImageAndReturnProductImage() {
    byte[] imageData = new byte[1024];
    ProductImage savedImage =
        ProductImage.builder()
            .id(1L)
            .productId(42L)
            .originalFilename("test.jpg")
            .contentType("image/jpeg")
            .originalSizeBytes(1024L)
            .storageKey("products/42/uuid")
            .build();

    when(imageRepositoryPort.save(any(ProductImage.class))).thenReturn(savedImage);
    when(storagePort.store(anyString(), any(ByteArrayInputStream.class), eq("image/webp")))
        .thenReturn("http://localhost/images/version.webp");

    Map<ImageSize, byte[]> versions =
        Map.of(
            ImageSize.MOBILE, new byte[100],
            ImageSize.TABLET, new byte[200],
            ImageSize.DESKTOP, new byte[300]);
    when(imageProcessingPort.processAllVersions(any(byte[].class), eq("image/jpeg")))
        .thenReturn(versions);

    ProductImage result = uploadService.upload(42L, "test.jpg", "image/jpeg", imageData);

    assertNotNull(result);
    assertEquals(42L, result.getProductId());
    assertEquals("test.jpg", result.getOriginalFilename());
    verify(imageProcessingPort).validate(imageData, "image/jpeg");
    verify(imageProcessingPort).processAllVersions(imageData, "image/jpeg");
    verify(storagePort, times(3))
        .store(anyString(), any(ByteArrayInputStream.class), eq("image/webp"));
    verify(imageRepositoryPort).save(any(ProductImage.class));
  }

  @Test
  void shouldGenerateStorageKeyWithProductIdAndUuid() {
    final byte[] imageData = new byte[1024];
    ProductImage savedImage =
        ProductImage.builder()
            .id(1L)
            .productId(42L)
            .originalFilename("test.jpg")
            .contentType("image/jpeg")
            .originalSizeBytes(1024L)
            .storageKey("products/42/some-uuid")
            .build();

    when(imageRepositoryPort.save(any(ProductImage.class))).thenReturn(savedImage);
    when(storagePort.store(anyString(), any(), eq("image/webp"))).thenReturn("url");
    when(imageProcessingPort.processAllVersions(any(byte[].class), anyString()))
        .thenReturn(
            Map.of(
                ImageSize.MOBILE, new byte[100],
                ImageSize.TABLET, new byte[200],
                ImageSize.DESKTOP, new byte[300]));

    uploadService.upload(42L, "test.jpg", "image/jpeg", imageData);

    verify(storagePort, atLeastOnce()).store(startsWith("products/42/"), any(), eq("image/webp"));
  }

  @Test
  void shouldRejectInvalidFileType() {
    byte[] imageData = new byte[100];
    doThrow(new InvalidImageException("Unsupported image format: image/gif"))
        .when(imageProcessingPort)
        .validate(imageData, "image/gif");

    assertThrows(
        InvalidImageException.class,
        () -> uploadService.upload(1L, "test.gif", "image/gif", imageData));

    verify(storagePort, never()).store(anyString(), any(), anyString());
    verify(imageRepositoryPort, never()).save(any());
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

    verify(storagePort, never()).store(anyString(), any(), anyString());
  }
}
