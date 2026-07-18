/* (C) 2026 */

package aros.services.rms.core.product.domain.event;

import java.time.Instant;

/**
 * Domain event published when a product is updated (including price changes).
 *
 * <p>Pure Java record — no Spring dependencies.
 */
public record ProductUpdatedEvent(Long productId, Instant occurredAt) {

  /**
   * Canonical constructor with validation.
   *
   * @param productId the ID of the updated product
   * @param occurredAt when the update occurred
   */
  public ProductUpdatedEvent {
    if (productId == null) {
      throw new IllegalArgumentException("productId must not be null");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt must not be null");
    }
  }
}
