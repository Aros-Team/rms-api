/* (C) 2026 */
package aros.services.rms.infraestructure.category.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import aros.services.rms.core.category.domain.Category;

@Schema(description = "Response DTO for category data")
public record CategoryResponse(
    @Schema(description = "Category ID", example = "1") Long id,
    @Schema(description = "Category name", example = "Hamburguesas") String name,
    @Schema(description = "Category description", example = "Todas las hamburguesas del menú")
        String description,
    @Schema(description = "Whether category is enabled", example = "true") boolean enabled) {

  public static CategoryResponse fromDomain(Category category) {
    if (category == null) return null;
    return new CategoryResponse(
        category.getId(), category.getName(), category.getDescription(), category.isEnabled());
  }
}