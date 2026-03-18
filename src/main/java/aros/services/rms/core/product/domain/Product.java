/* (C) 2026 */
package aros.services.rms.core.product.domain;

import aros.services.rms.core.category.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a product in the restaurant menu. Products are linked to a preparation
 * area and a category. The hasOptions flag determines if customization options can be associated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
  private Long id;
  private String name;
  private Double basePrice;
  private boolean hasOptions;
  @Builder.Default private boolean active = true;
  private Category category;
  private Long preparationAreaId;
  private String preparationAreaName;
}
