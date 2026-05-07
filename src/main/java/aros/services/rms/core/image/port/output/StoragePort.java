package aros.services.rms.core.image.port.output;

import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;

/** Output port for image storage operations (local or GCS). */
public interface StoragePort {
  /** Stores image data and returns its access URL. */
  String store(String key, InputStream data, String contentType);

  /** Loads image data by key. Returns empty if not found. */
  Optional<InputStream> load(String key);

  /** Deletes image data by key. */
  void delete(String key);

  /** Generates a signed URL for temporary access. */
  String generateSignedUrl(String key, Duration expiration);
}
