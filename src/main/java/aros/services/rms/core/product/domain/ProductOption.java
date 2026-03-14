/* (C) 2026 */
package aros.services.rms.core.product.domain;

import aros.services.rms.core.category.domain.OptionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOption {
  private Long id;
  private String name;
  private OptionCategory category;
  private Product product;
}
