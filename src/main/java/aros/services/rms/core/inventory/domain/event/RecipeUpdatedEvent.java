/* (C) 2026 */

package aros.services.rms.core.inventory.domain.event;

import java.time.Instant;

/**
 * Domain event published when a product's recipe is updated (add/modify/delete recipe rows).
 *
 * <p>Pure Java record — no Spring dependencies.
 */
public record RecipeUpdatedEvent(Long productId, Instant occurredAt) {

  /**
   * Canonical constructor with validation.
   *
   * @param productId the ID of the product whose recipe changed
   * @param occurredAt when the change occurred
   */
  public RecipeUpdatedEvent {
    if (productId == null) {
      throw new IllegalArgumentException("productId must not be null");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt must not be null");
    }
  }
}
