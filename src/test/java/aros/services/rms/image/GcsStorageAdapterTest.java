/* (C) 2026 */

package aros.services.rms.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.infraestructure.image.storage.StorageProperties;
import aros.services.rms.infraestructure.image.storage.gcs.GcsStorageAdapter;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GcsStorageAdapterTest {

  @Mock private Storage storage;

  @Mock private StorageProperties storageProperties;

  @Mock private StorageProperties.Gcs gcsProperties;

  private GcsStorageAdapter adapter;

  @BeforeEach
  void setup() {
    lenient().when(storageProperties.getGcs()).thenReturn(gcsProperties);
    lenient().when(gcsProperties.getBucket()).thenReturn("test-bucket");
    lenient().when(gcsProperties.getSignedUrlExpirationMinutes()).thenReturn(60);
    adapter = new GcsStorageAdapter(storage, storageProperties);
  }

  @Test
  void shouldStoreFileInGcs() {
    byte[] bytes = {1, 2, 3};
    InputStream data = new ByteArrayInputStream(bytes);
    String key = "products/1/uuid/mobile.webp";

    String result = adapter.store(key, data, "image/webp");

    assertEquals(key, result);
    verify(storage).create(any(BlobInfo.class), any(byte[].class));
  }

  @Test
  void shouldLoadFileFromGcs() {
    byte[] content = {5, 6, 7};
    Blob blob = mock(Blob.class);
    when(blob.getContent()).thenReturn(content);
    when(storage.get(BlobId.of("test-bucket", "products/1/uuid/mobile.webp"))).thenReturn(blob);

    Optional<InputStream> result = adapter.load("products/1/uuid/mobile.webp");

    assertTrue(result.isPresent());
  }

  @Test
  void shouldReturnEmptyWhenFileNotFoundInGcs() {
    when(storage.get(BlobId.of("test-bucket", "missing"))).thenReturn(null);

    Optional<InputStream> result = adapter.load("missing");

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldDeleteFileFromGcs() {
    adapter.delete("products/1/uuid/mobile.webp");
    verify(storage).delete(BlobId.of("test-bucket", "products/1/uuid/mobile.webp"));
  }

  @Test
  void shouldGenerateSignedUrl() throws Exception {
    URL url =
        new URL("https://storage.googleapis.com/test-bucket/products/1/uuid/mobile.webp?sig=abc");
    when(storage.signUrl(
            any(BlobInfo.class),
            anyLong(),
            any(TimeUnit.class),
            any(Storage.SignUrlOption[].class)))
        .thenReturn(url);

    String result =
        adapter.generateSignedUrl("products/1/uuid/mobile.webp", Duration.ofMinutes(60));

    assertNotNull(result);
    verify(storage)
        .signUrl(
            any(BlobInfo.class), eq(60L), eq(TimeUnit.MINUTES), any(Storage.SignUrlOption[].class));
  }
}
