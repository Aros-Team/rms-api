package aros.services.rms.infraestructure.image.processing;

import aros.services.rms.core.image.application.exception.InvalidImageException;
import aros.services.rms.core.image.domain.ImageFormat;
import aros.services.rms.core.image.domain.ImageSize;
import aros.services.rms.core.image.port.output.ImageProcessingPort;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Image processing adapter using Scrimage library. Implements ImageProcessingPort to provide image
 * resizing, format conversion to WebP, and validation.
 */
public class ScrimeImageProcessor implements ImageProcessingPort {

  private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

  @Override
  public Map<ImageSize, byte[]> processAllVersions(byte[] imageData, String contentType) {
    validate(imageData, contentType);

    Map<ImageSize, byte[]> versions = new LinkedHashMap<>();
    for (ImageSize size : ImageSize.values()) {
      versions.put(size, processVersion(imageData, size, contentType));
    }
    return versions;
  }

  @Override
  public byte[] processVersion(byte[] imageData, ImageSize size, String contentType) {
    validate(imageData, contentType);

    try {
      ImmutableImage image = ImmutableImage.loader().fromBytes(imageData);

      int targetWidth = Math.min(size.maxWidth, image.width);

      ImmutableImage resized = image.scaleToWidth(targetWidth);
      return resized.bytes(WebpWriter.DEFAULT.withQ(size.quality));
    } catch (IOException e) {
      throw new InvalidImageException("No se pudo procesar la imagen: " + e.getMessage());
    }
  }

  @Override
  public void validate(byte[] imageData, String contentType) {
    if (imageData == null || imageData.length == 0) {
      throw new InvalidImageException("Los datos de la imagen están vacíos");
    }
    if (imageData.length > MAX_FILE_SIZE_BYTES) {
      throw new InvalidImageException("Image size exceeds maximum allowed size of 5MB");
    }
    if (!ImageFormat.isSupported(contentType)) {
      throw new InvalidImageException("Formato de imagen no soportado: " + contentType);
    }
  }
}
