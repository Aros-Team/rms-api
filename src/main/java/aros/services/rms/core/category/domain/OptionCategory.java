/* (C) 2026 */

package aros.services.rms.core.category.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing an option category for product customization (e.g., "Cooking term",
 * "Milk type"). These define what can be added to or modified in a product.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionCategory {
  private Long id;
  private String name;
  private String description;
}
