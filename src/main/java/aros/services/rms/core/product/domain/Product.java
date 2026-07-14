/* (C) 2026 */

package aros.services.rms.core.product.domain;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.specialselection.domain.SelectionType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a sellable product in the catalog, including pricing, category, preparation area and
 * optional selection type (standard or special selection).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
  private Long id;
  private String name;
  private String description;
  private Double basePrice;
  @Builder.Default private boolean active = true;
  private Category category;
  private Long preparationAreaId;
  private String preparationAreaName;
  private List<ProductRecipe> recipe;
  private List<Long> optionIds;
  @Builder.Default private SelectionType selectionType = SelectionType.STANDARD;
  @Builder.Default private boolean baseRecipeEnabled = false;
  @Builder.Default private boolean schedulingRequired = false;
  private Integer estimatedPrepMinutes;
}
