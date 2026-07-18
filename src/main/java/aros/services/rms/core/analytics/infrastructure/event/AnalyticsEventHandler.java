/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.event;

import aros.services.rms.core.analytics.domain.port.out.MenuEngineeringCacheRepositoryPort;
import aros.services.rms.core.inventory.domain.event.RecipeUpdatedEvent;
import aros.services.rms.core.product.domain.event.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Handles domain events related to product and recipe changes by invalidating the menu engineering
 * cache.
 */
@Component
@RequiredArgsConstructor
public class AnalyticsEventHandler {

  private static final Logger log = LoggerFactory.getLogger(AnalyticsEventHandler.class);

  private final MenuEngineeringCacheRepositoryPort cacheRepo;

  /**
   * When a product is updated (including price change), invalidate its cache rows.
   *
   * @param event the product updated event
   */
  @TransactionalEventListener
  public void onProductUpdated(ProductUpdatedEvent event) {
    log.info(
        "Cache invalidation: product updated, deleting cache rows for productId={}",
        event.productId());
    cacheRepo.deleteByProductId(event.productId());
  }

  /**
   * When a recipe is updated, invalidate the product's cache rows.
   *
   * @param event the recipe updated event
   */
  @TransactionalEventListener
  public void onRecipeUpdated(RecipeUpdatedEvent event) {
    log.info(
        "Cache invalidation: recipe updated, deleting cache rows for productId={}",
        event.productId());
    cacheRepo.deleteByProductId(event.productId());
  }
}
