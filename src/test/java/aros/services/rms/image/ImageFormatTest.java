package aros.services.rms.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aros.services.rms.core.image.domain.ImageFormat;
import org.junit.jupiter.api.Test;

class ImageFormatTest {

  @Test
  void shouldRecognizeSupportedMimeTypes() {
    assertTrue(ImageFormat.isSupported("image/jpeg"));
    assertTrue(ImageFormat.isSupported("image/png"));
    assertTrue(ImageFormat.isSupported("image/webp"));
  }

  @Test
  void shouldRejectUnsupportedMimeTypes() {
    assertFalse(ImageFormat.isSupported("image/gif"));
    assertFalse(ImageFormat.isSupported("image/bmp"));
    assertFalse(ImageFormat.isSupported("image/svg+xml"));
  }

  @Test
  void shouldReturnCorrectFormatFromMimeType() {
    assertEquals(ImageFormat.JPEG, ImageFormat.fromMimeType("image/jpeg").orElseThrow());
    assertEquals(ImageFormat.PNG, ImageFormat.fromMimeType("image/png").orElseThrow());
    assertEquals(ImageFormat.WEBP, ImageFormat.fromMimeType("image/webp").orElseThrow());
  }

  @Test
  void shouldReturnEmptyForUnknownMimeType() {
    assertTrue(ImageFormat.fromMimeType("image/gif").isEmpty());
  }

  @Test
  void shouldHaveCorrectExtensions() {
    assertTrue(ImageFormat.JPEG.extensions.contains("jpg"));
    assertTrue(ImageFormat.JPEG.extensions.contains("jpeg"));
    assertTrue(ImageFormat.PNG.extensions.contains("png"));
    assertTrue(ImageFormat.WEBP.extensions.contains("webp"));
  }

  @Test
  void shouldHandleCaseInsensitiveMimeType() {
    assertTrue(ImageFormat.isSupported("IMAGE/JPEG"));
    assertTrue(ImageFormat.isSupported("Image/Png"));
  }
}
