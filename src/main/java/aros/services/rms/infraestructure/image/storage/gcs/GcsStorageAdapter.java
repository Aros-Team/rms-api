/* (C) 2026 */

package aros.services.rms.infraestructure.image.storage.gcs;

import aros.services.rms.core.image.port.output.StoragePort;
import aros.services.rms.infraestructure.image.storage.StorageProperties;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.SignUrlOption;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** GCS storage adapter for production profile. Uses core google-cloud-storage library. */
@Component
@Profile("production")
public class GcsStorageAdapter implements StoragePort {

  private final Storage storage;
  private final StorageProperties storageProperties;

  /** Creates adapter with storage properties. Creates Storage client using default credentials. */
  public GcsStorageAdapter(StorageProperties storageProperties) {
    this.storage = StorageOptions.getDefaultInstance().getService();
    this.storageProperties = storageProperties;
  }

  /** Creates adapter with custom storage client. For testing. */
  public GcsStorageAdapter(Storage storage, StorageProperties storageProperties) {
    this.storage = storage;
    this.storageProperties = storageProperties;
  }

  /** Stores file in GCS bucket and returns its key. */
  @Override
  public String store(String key, InputStream data, String contentType) {
    try {
      byte[] bytes = data.readAllBytes();
      BlobInfo blobInfo =
          BlobInfo.newBuilder(storageProperties.getGcs().getBucket(), key)
              .setContentType(contentType)
              .build();
      storage.create(blobInfo, bytes);
      return key;
    } catch (IOException e) {
      throw new RuntimeException("Failed to store file in GCS: " + key, e);
    }
  }

  /** Loads file from GCS. Returns empty if not found. */
  @Override
  public Optional<InputStream> load(String key) {
    Blob blob = storage.get(BlobId.of(storageProperties.getGcs().getBucket(), key));
    if (blob == null) {
      return Optional.empty();
    }
    return Optional.of(new ByteArrayInputStream(blob.getContent()));
  }

  /** Deletes file from GCS bucket. */
  @Override
  public void delete(String key) {
    storage.delete(BlobId.of(storageProperties.getGcs().getBucket(), key));
  }

  /** Generates signed URL with v4 signature. */
  @Override
  public String generateSignedUrl(String key, Duration expiration) {
    BlobInfo blobInfo = BlobInfo.newBuilder(storageProperties.getGcs().getBucket(), key).build();
    long expirationMinutes = expiration.toMinutes();
    URL url =
        storage.signUrl(
            blobInfo, expirationMinutes, TimeUnit.MINUTES, SignUrlOption.withV4Signature());
    return url.toString();
  }
}
