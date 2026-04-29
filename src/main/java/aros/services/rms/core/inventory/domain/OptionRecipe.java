/* (C) 2026 */

package aros.services.rms.core.inventory.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for option recipe - defines what supply variants are needed for a product option.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionRecipe {

  private Long id;
  private Long optionId;
  private Long supplyVariantId;
  private BigDecimal requiredQuantity;
}
