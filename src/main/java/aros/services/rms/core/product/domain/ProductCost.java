/* (C) 2026 */

package aros.services.rms.core.product.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Result of calculating the production cost of a product on-the-fly. Includes material costs (from
 * recipe × supply unit costs) and labor costs (from worker hourly rates × estimated prep time).
 */
public record ProductCost(
    Long productId,
    BigDecimal totalCost,
    BigDecimal materialCost,
    BigDecimal laborCost,
    List<CostBreakdownItem> breakdown) {

  /** A single line in the cost breakdown describing one component (e.g., a material or labor). */
  public record CostBreakdownItem(String description, BigDecimal amount, String type) {

    /**
     * Canonical constructor with validation.
     *
     * @param description the human-readable description
     * @param amount the amount (must not be null)
     * @param type the cost type (must not be blank)
     */
    public CostBreakdownItem {
      if (amount == null) {
        throw new IllegalArgumentException("amount must not be null");
      }
      if (description == null || description.isBlank()) {
        throw new IllegalArgumentException("description must not be blank");
      }
      if (type == null || type.isBlank()) {
        throw new IllegalArgumentException("type must not be blank");
      }
    }
  }

  /**
   * Canonical constructor with validation.
   *
   * @param productId the product identifier
   * @param totalCost total production cost
   * @param materialCost material cost component
   * @param laborCost labor cost component
   * @param breakdown cost breakdown items
   */
  public ProductCost {
    if (productId == null) {
      throw new IllegalArgumentException("productId must not be null");
    }
    if (totalCost == null) {
      throw new IllegalArgumentException("totalCost must not be null");
    }
    if (materialCost == null) {
      throw new IllegalArgumentException("materialCost must not be null");
    }
    if (laborCost == null) {
      throw new IllegalArgumentException("laborCost must not be null");
    }
    if (breakdown == null) {
      throw new IllegalArgumentException("breakdown must not be null");
    }
  }
}
