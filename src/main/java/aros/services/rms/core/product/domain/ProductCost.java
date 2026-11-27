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
    List<CostBreakdownItem> breakdown,
    Integer estimatedPrepMinutes,
    BigDecimal realPrepMinutes,
    long realPrepSampleCount,
    PrepTimeMode prepTimeMode,
    PrepTimeMode prepTimeUsedSource,
    BigDecimal prepTimeUsedMinutes) {

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
        throw new IllegalArgumentException("amount no puede ser nulo");
      }
      if (description == null || description.isBlank()) {
        throw new IllegalArgumentException("La descripción no puede estar vacía");
      }
      if (type == null || type.isBlank()) {
        throw new IllegalArgumentException("El tipo no puede estar vacío");
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
      throw new IllegalArgumentException("productId no puede ser nulo");
    }
    if (totalCost == null) {
      throw new IllegalArgumentException("totalCost no puede ser nulo");
    }
    if (materialCost == null) {
      throw new IllegalArgumentException("materialCost no puede ser nulo");
    }
    if (laborCost == null) {
      throw new IllegalArgumentException("laborCost no puede ser nulo");
    }
    if (breakdown == null) {
      throw new IllegalArgumentException("breakdown no puede ser nulo");
    }
  }
}
