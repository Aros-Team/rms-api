package aros.services.rms.image;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aros.services.rms.infraestructure.image.storage.StorageProperties;
import aros.services.rms.infraestructure.image.storage.local.LocalStorageAdapter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalStorageAdapterTest {

  @TempDir Path tempDir;

  private LocalStorageAdapter storageAdapter;
  private StorageProperties storageProperties;

  @BeforeEach
  void setUp() {
    storageProperties = new StorageProperties();
    storageProperties.getLocal().setBaseDir(tempDir.toString());
    storageProperties.getLocal().setBaseUrl("http://localhost:8080/api/v1/images/local");
    storageAdapter = new LocalStorageAdapter(storageProperties);
  }

  @Test
  void shouldStoreFileAndReturnUrl() {
    byte[] data = "test image data".getBytes();
    InputStream inputStream = new ByteArrayInputStream(data);

    String url = storageAdapter.store("products/1/uuid/mobile.webp", inputStream, "image/webp");

    assertTrue(url.contains("products/1/uuid/mobile.webp"));
    assertTrue(Files.exists(tempDir.resolve("products/1/uuid/mobile.webp")));
  }

  @Test
  void shouldLoadStoredFile() throws IOException {
    byte[] data = "test image data".getBytes();
    InputStream inputStream = new ByteArrayInputStream(data);
    storageAdapter.store("products/1/uuid/mobile.webp", inputStream, "image/webp");

    Optional<InputStream> loaded = storageAdapter.load("products/1/uuid/mobile.webp");

    assertTrue(loaded.isPresent());
    byte[] loadedData = loaded.get().readAllBytes();
    assertArrayEquals(data, loadedData);
  }

  @Test
  void shouldReturnEmptyForNonExistentFile() {
    Optional<InputStream> loaded = storageAdapter.load("nonexistent.webp");
    assertTrue(loaded.isEmpty());
  }

  @Test
  void shouldDeleteFile() {
    byte[] data = "test image data".getBytes();
    InputStream inputStream = new ByteArrayInputStream(data);
    storageAdapter.store("products/1/uuid/mobile.webp", inputStream, "image/webp");

    storageAdapter.delete("products/1/uuid/mobile.webp");

    assertTrue(Files.notExists(tempDir.resolve("products/1/uuid/mobile.webp")));
  }

  @Test
  void shouldNotThrowWhenDeletingNonExistentFile() {
    assertDoesNotThrow(() -> storageAdapter.delete("nonexistent.webp"));
  }

  @Test
  void shouldGenerateLocalUrl() {
    String url =
        storageAdapter.generateSignedUrl("products/1/uuid/mobile.webp", Duration.ofMinutes(60));
    assertEquals("http://localhost:8080/api/v1/images/local/products/1/uuid/mobile.webp", url);
  }

  @Test
  void shouldCreateSubdirectoriesWhenStoring() {
    byte[] data = "test image data".getBytes();
    InputStream inputStream = new ByteArrayInputStream(data);

    String url = storageAdapter.store("products/999/abc123/mobile.webp", inputStream, "image/webp");

    assertNotNull(url);
    assertTrue(Files.exists(tempDir.resolve("products/999/abc123/mobile.webp")));
  }
}
