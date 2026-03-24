/* (C) 2026 */
package aros.services.rms.core.inventory.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model for product recipe - defines what supply variants are needed for a product. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecipe {

  private Long id;
  private Long productId;
  private Long supplyVariantId;
  private BigDecimal requiredQuantity;
}
