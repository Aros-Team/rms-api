package aros.services.rms.infraestructure.image.storage.local;

import aros.services.rms.core.image.port.output.StoragePort;
import aros.services.rms.infraestructure.image.storage.StorageProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Local filesystem storage adapter for development profile. */
@Component
@Profile("development")
public class LocalStorageAdapter implements StoragePort {

  private static final Logger log = LoggerFactory.getLogger(LocalStorageAdapter.class);
  private final Path baseDir;
  private final String baseUrl;

  /** Creates adapter with storage properties. */
  public LocalStorageAdapter(StorageProperties storageProperties) {
    this.baseDir = Paths.get(storageProperties.getLocal().getBaseDir());
    this.baseUrl = storageProperties.getLocal().getBaseUrl();
    ensureBaseDirExists();
  }

  /** Stores file locally and returns its URL. */
  @Override
  public String store(String key, InputStream data, String contentType) {
    try {
      Path filePath = baseDir.resolve(key);
      Files.createDirectories(filePath.getParent());
      Files.copy(data, filePath, StandardCopyOption.REPLACE_EXISTING);
      log.info("Stored file locally at: {}", filePath);
      return baseUrl + "/" + key;
    } catch (IOException e) {
      throw new RuntimeException("Failed to store file locally: " + key, e);
    }
  }

  /** Loads file from local filesystem. Returns empty if not found. */
  @Override
  public Optional<InputStream> load(String key) {
    try {
      Path filePath = baseDir.resolve(key);
      if (Files.exists(filePath)) {
        return Optional.of(Files.newInputStream(filePath));
      }
      return Optional.empty();
    } catch (IOException e) {
      throw new RuntimeException("Failed to load file locally: " + key, e);
    }
  }

  /** Deletes file from local filesystem. Ignores if not found. */
  @Override
  public void delete(String key) {
    try {
      Path filePath = baseDir.resolve(key);
      Files.deleteIfExists(filePath);
      log.info("Deleted file locally at: {}", filePath);
    } catch (IOException e) {
      log.warn("Failed to delete file locally: {}", key, e);
    }
  }

  /** Returns local URL (no signing needed for dev). */
  @Override
  public String generateSignedUrl(String key, Duration expiration) {
    // In dev mode, return a direct URL (no signing needed for local storage)
    return baseUrl + "/" + key;
  }

  private void ensureBaseDirExists() {
    try {
      Files.createDirectories(baseDir);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create base upload directory: " + baseDir, e);
    }
  }
}
