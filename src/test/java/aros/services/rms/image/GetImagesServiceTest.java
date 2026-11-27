/* (C) 2026 */

package aros.services.rms.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.image.application.service.GetImagesService;
import aros.services.rms.core.image.domain.EntityImage;
import aros.services.rms.core.image.domain.ImageEntityType;
import aros.services.rms.core.image.domain.ImageWithUrls;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import aros.services.rms.core.image.port.output.StoragePort;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link GetImagesService#getFirstByEntities}.
 *
 * <p>Feature: batch resolution of worker portraits, used to enrich the worker list in a single
 * query instead of one request per card.
 */
class GetImagesServiceTest {

  private ImageRepositoryPort imageRepositoryPort;
  private StoragePort storagePort;
  private GetImagesService service;

  @BeforeEach
  void setUp() {
    imageRepositoryPort = mock(ImageRepositoryPort.class);
    storagePort = mock(StoragePort.class);
    service = new GetImagesService(imageRepositoryPort, storagePort, mock(Logger.class));
    when(storagePort.generateSignedUrl(anyString(), any()))
        .thenAnswer(invocation -> "url:" + invocation.getArgument(0));
  }

  @Test
  void shouldReturnOnlyTheFirstImagePerEntity() {
    EntityImage first = image(1L, 10L, "users/10/a");
    EntityImage second = image(2L, 10L, "users/10/b");
    EntityImage other = image(3L, 11L, "users/11/a");
    when(imageRepositoryPort.findByEntityTypeAndEntityIds(ImageEntityType.USER, List.of(10L, 11L)))
        .thenReturn(List.of(first, second, other));

    Map<Long, ImageWithUrls> result =
        service.getFirstByEntities(ImageEntityType.USER, List.of(10L, 11L));

    assertEquals(2, result.size());
    assertEquals(1L, result.get(10L).getImage().getId());
    assertEquals("url:users/10/a/tablet.webp", result.get(10L).getTabletUrl());
    assertEquals(3L, result.get(11L).getImage().getId());
  }

  @Test
  void shouldSkipTheQueryWhenThereAreNoEntityIds() {
    assertTrue(service.getFirstByEntities(ImageEntityType.USER, List.of()).isEmpty());

    verifyNoInteractions(imageRepositoryPort);
  }

  private static EntityImage image(long id, long entityId, String storageKey) {
    return EntityImage.builder()
        .id(id)
        .entityType(ImageEntityType.USER)
        .entityId(entityId)
        .storageKey(storageKey)
        .originalFilename("seed-worker-" + entityId + ".jpg")
        .contentType("image/jpeg")
        .originalSizeBytes(10L)
        .build();
  }
}
