package aros.services.rms.core.image.domain;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/** Supported image formats for upload and processing. */
public enum ImageFormat {
  JPEG("image/jpeg", Set.of("jpg", "jpeg")),
  PNG("image/png", Set.of("png")),
  WEBP("image/webp", Set.of("webp"));

  public final String mimeType;
  public final Set<String> extensions;

  ImageFormat(String mimeType, Set<String> extensions) {
    this.mimeType = mimeType;
    this.extensions = extensions;
  }

  /** Finds an ImageFormat by its MIME type. */
  public static Optional<ImageFormat> fromMimeType(String mimeType) {
    return Arrays.stream(values()).filter(f -> f.mimeType.equalsIgnoreCase(mimeType)).findFirst();
  }

  /** Checks if the given MIME type is a supported image format. */
  public static boolean isSupported(String mimeType) {
    return fromMimeType(mimeType).isPresent();
  }
}
