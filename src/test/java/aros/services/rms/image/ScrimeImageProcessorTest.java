package aros.services.rms.image;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aros.services.rms.core.image.application.exception.InvalidImageException;
import aros.services.rms.core.image.domain.ImageSize;
import aros.services.rms.infraestructure.image.processing.ScrimeImageProcessor;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.JpegWriter;
import com.sksamuel.scrimage.nio.PngWriter;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ScrimeImageProcessorTest {

  private ScrimeImageProcessor processor;

  @BeforeEach
  void setUp() {
    processor = new ScrimeImageProcessor();
  }

  private byte[] createTestJpeg(int width, int height) throws IOException {
    ImmutableImage image = ImmutableImage.create(width, height);
    return image.bytes(new JpegWriter());
  }

  private byte[] createTestPng(int width, int height) throws IOException {
    ImmutableImage image = ImmutableImage.create(width, height);
    return image.bytes(PngWriter.NoCompression);
  }

  @Test
  void shouldValidateSupportedMimeTypeJpeg() throws IOException {
    byte[] imageData = createTestJpeg(100, 100);
    assertDoesNotThrow(() -> processor.validate(imageData, "image/jpeg"));
  }

  @Test
  void shouldValidateSupportedMimeTypePng() throws IOException {
    byte[] imageData = createTestPng(100, 100);
    assertDoesNotThrow(() -> processor.validate(imageData, "image/png"));
  }

  @Test
  void shouldValidateSupportedMimeTypeWebP() throws IOException {
    byte[] imageData = createTestJpeg(100, 100);
    assertDoesNotThrow(() -> processor.validate(imageData, "image/webp"));
  }

  @Test
  void shouldRejectInvalidFormatBmp() {
    byte[] imageData = new byte[100];
    assertThrows(InvalidImageException.class, () -> processor.validate(imageData, "image/bmp"));
  }

  @Test
  void shouldRejectInvalidFormatGif() {
    byte[] imageData = new byte[100];
    assertThrows(InvalidImageException.class, () -> processor.validate(imageData, "image/gif"));
  }

  @Test
  void shouldRejectExceedingFileSize() {
    byte[] imageData = new byte[6 * 1024 * 1024];
    assertThrows(InvalidImageException.class, () -> processor.validate(imageData, "image/jpeg"));
  }

  @Test
  void shouldAcceptFileSizeAtLimit() {
    byte[] imageData = new byte[4 * 1024 * 1024];
    assertDoesNotThrow(() -> processor.validate(imageData, "image/jpeg"));
  }

  @Test
  void shouldResizeImageToMobileWidth() throws IOException {
    byte[] imageData = createTestJpeg(1920, 1080);
    byte[] result = processor.processVersion(imageData, ImageSize.MOBILE, "image/jpeg");

    ImmutableImage output = ImmutableImage.loader().fromBytes(result);
    assertEquals(400, output.width);
    assertTrue(
        Math.abs(225 - output.height) <= 1,
        "Height should preserve aspect ratio, got: " + output.height);
  }

  @Test
  void shouldResizeImageToTabletWidth() throws IOException {
    byte[] imageData = createTestJpeg(1920, 1080);
    byte[] result = processor.processVersion(imageData, ImageSize.TABLET, "image/jpeg");

    ImmutableImage output = ImmutableImage.loader().fromBytes(result);
    assertEquals(800, output.width);
    assertTrue(
        Math.abs(450 - output.height) <= 1,
        "Height should preserve aspect ratio, got: " + output.height);
  }

  @Test
  void shouldResizeImageToDesktopWidth() throws IOException {
    byte[] imageData = createTestJpeg(1920, 1080);
    byte[] result = processor.processVersion(imageData, ImageSize.DESKTOP, "image/jpeg");

    ImmutableImage output = ImmutableImage.loader().fromBytes(result);
    assertEquals(1200, output.width);
    assertTrue(
        Math.abs(675 - output.height) <= 1,
        "Height should preserve aspect ratio, got: " + output.height);
  }

  @Test
  void shouldNotUpscaleImage() throws IOException {
    byte[] imageData = createTestJpeg(300, 200);
    byte[] result = processor.processVersion(imageData, ImageSize.DESKTOP, "image/jpeg");

    ImmutableImage output = ImmutableImage.loader().fromBytes(result);
    assertEquals(300, output.width, "Should not upscale beyond original width");
  }

  @Test
  void shouldNotUpscaleMobileWhenSourceSmaller() throws IOException {
    byte[] imageData = createTestJpeg(200, 150);
    byte[] result = processor.processVersion(imageData, ImageSize.MOBILE, "image/jpeg");

    ImmutableImage output = ImmutableImage.loader().fromBytes(result);
    assertEquals(200, output.width, "Should not upscale beyond original width");
  }

  @Test
  void shouldGenerateAllThreeVersions() throws IOException {
    byte[] imageData = createTestJpeg(1920, 1080);
    Map<ImageSize, byte[]> results = processor.processAllVersions(imageData, "image/jpeg");

    assertEquals(3, results.size());
    assertTrue(results.containsKey(ImageSize.MOBILE));
    assertTrue(results.containsKey(ImageSize.TABLET));
    assertTrue(results.containsKey(ImageSize.DESKTOP));
    assertTrue(results.get(ImageSize.MOBILE).length > 0);
    assertTrue(results.get(ImageSize.TABLET).length > 0);
    assertTrue(results.get(ImageSize.DESKTOP).length > 0);
  }

  @Test
  void shouldConvertJpegToWebP() throws IOException {
    byte[] jpegData = createTestJpeg(800, 600);
    byte[] result = processor.processVersion(jpegData, ImageSize.MOBILE, "image/jpeg");

    assertNotNull(result);
    assertTrue(result.length > 4);
    String header = new String(result, 0, 4, "ASCII");
    assertEquals("RIFF", header, "Output should be in WebP format");
  }

  @Test
  void shouldConvertPngToWebP() throws IOException {
    byte[] pngData = createTestPng(800, 600);
    byte[] result = processor.processVersion(pngData, ImageSize.MOBILE, "image/png");

    assertNotNull(result);
    assertTrue(result.length > 4);
    String header = new String(result, 0, 4, "ASCII");
    assertEquals("RIFF", header, "Output should be in WebP format");
  }

  @Test
  @Disabled("Animated WebP detection not yet implemented")
  void shouldRejectAnimatedWebP() {
    // TODO: Create animated WebP test data and verify rejection
  }
}
