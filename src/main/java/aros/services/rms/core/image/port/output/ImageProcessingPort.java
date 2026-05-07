package aros.services.rms.core.image.port.output;

import aros.services.rms.core.image.domain.ImageSize;
import java.util.Map;

/** Output port for image processing operations. */
public interface ImageProcessingPort {
  /** Processes image into all size versions. */
  Map<ImageSize, byte[]> processAllVersions(byte[] imageData, String contentType);

  /** Processes image into a single size version. */
  byte[] processVersion(byte[] imageData, ImageSize size, String contentType);

  /** Validates image data and content type. */
  void validate(byte[] imageData, String contentType);
}
