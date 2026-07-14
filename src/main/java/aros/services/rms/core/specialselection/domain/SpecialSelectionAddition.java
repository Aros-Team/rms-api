package aros.services.rms.core.specialselection.domain;

import aros.services.rms.core.product.domain.ProductOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an optional addition that can be applied to a special selection, including its extra
 * price and display ordering.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialSelectionAddition {
  private Long id;
  private Long productId;
  private Long optionId;
  private ProductOption option;
  private String name;
  private Double extraPrice;
  private int displayOrder;
}
