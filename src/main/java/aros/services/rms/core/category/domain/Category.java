/* (C) 2026 */
package aros.services.rms.core.category.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a product category (e.g., Entradas, Fuertes, Bebidas). These categories
 * classify what a product IS, not what can be added to it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
  private Long id;
  private String name;
  private String description;
  @Builder.Default private boolean enabled = true;
}
