package aros.services.rms.image;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.image.application.exception.ProductImageNotFoundException;
import aros.services.rms.core.image.application.service.DeleteProductImageService;
import aros.services.rms.core.image.domain.ProductImage;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteProductImageServiceTest {

  @Mock private ImageRepositoryPort imageRepositoryPort;

  @Mock private StoragePort storagePort;

  @Mock private Logger logger;

  private DeleteProductImageService deleteService;

  @BeforeEach
  void setUp() {
    deleteService = new DeleteProductImageService(imageRepositoryPort, storagePort, logger);
  }

  @Test
  void shouldDeleteImageFromStorageAndDatabase() {
    ProductImage image =
        ProductImage.builder().id(1L).productId(42L).storageKey("products/42/uuid").build();

    when(imageRepositoryPort.findById(1L)).thenReturn(Optional.of(image));

    deleteService.delete(1L);

    verify(storagePort, times(3)).delete(anyString());
    verify(storagePort).delete(contains("mobile.webp"));
    verify(storagePort).delete(contains("tablet.webp"));
    verify(storagePort).delete(contains("desktop.webp"));
    verify(imageRepositoryPort).deleteById(1L);
  }

  @Test
  void shouldThrowWhenImageNotFound() {
    when(imageRepositoryPort.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ProductImageNotFoundException.class, () -> deleteService.delete(999L));
    verify(storagePort, never()).delete(anyString());
    verify(imageRepositoryPort, never()).deleteById(anyLong());
  }

  @Test
  void shouldContinueIfStorageDeleteFails() {
    ProductImage image =
        ProductImage.builder().id(1L).productId(42L).storageKey("products/42/uuid").build();

    when(imageRepositoryPort.findById(1L)).thenReturn(Optional.of(image));
    doThrow(new RuntimeException("Storage error")).when(storagePort).delete(anyString());

    assertDoesNotThrow(() -> deleteService.delete(1L));
    verify(imageRepositoryPort).deleteById(1L);
  }

  @Test
  void shouldDeleteAllThreeVersionsFromStorage() {
    ProductImage image =
        ProductImage.builder().id(1L).productId(42L).storageKey("products/42/abc").build();

    when(imageRepositoryPort.findById(1L)).thenReturn(Optional.of(image));

    deleteService.delete(1L);

    verify(storagePort).delete("products/42/abc/mobile.webp");
    verify(storagePort).delete("products/42/abc/tablet.webp");
    verify(storagePort).delete("products/42/abc/desktop.webp");
  }
}
