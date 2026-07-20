/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringCacheRepositoryPort;
import aros.services.rms.core.inventory.domain.event.RecipeUpdatedEvent;
import aros.services.rms.core.product.domain.event.ProductUpdatedEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AnalyticsEventHandlerTest {

  private final MenuEngineeringCacheRepositoryPort cacheRepo =
      mock(MenuEngineeringCacheRepositoryPort.class);
  private final AnalyticsEventHandler handler = new AnalyticsEventHandler(cacheRepo);

  @Test
  void shouldInvalidateCacheOnProductUpdated() {
    ProductUpdatedEvent event = new ProductUpdatedEvent(42L, Instant.now());

    handler.onProductUpdated(event);

    verify(cacheRepo).deleteByProductId(42L);
  }

  @Test
  void shouldInvalidateCacheOnRecipeUpdated() {
    RecipeUpdatedEvent event = new RecipeUpdatedEvent(99L, Instant.now());

    handler.onRecipeUpdated(event);

    verify(cacheRepo).deleteByProductId(99L);
  }
}
