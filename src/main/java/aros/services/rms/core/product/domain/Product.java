/* (C) 2026 */

package aros.services.rms.core.product.domain;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a product in the restaurant menu. Products are linked to a preparation
 * area and a category. Options can be associated freely without any flag restriction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
  private Long id;
  private String name;
  private Double basePrice;
  @Builder.Default private boolean active = true;
  private Category category;
  private Long preparationAreaId;
  private String preparationAreaName;
  private List<ProductRecipe> recipe;
  private List<Long> optionIds;
}
