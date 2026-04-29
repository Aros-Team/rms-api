/* (C) 2026 */
package aros.services.rms.infraestructure.category.api.dto;

import aros.services.rms.core.category.domain.Category;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for category data. */
@Schema(description = "Response DTO for category data")
public record CategoryResponse(
    @Schema(description = "Category ID", example = "1") Long id,
    @Schema(description = "Category name", example = "Hamburguesas") String name,
    @Schema(description = "Category description", example = "Todas las hamburguesas del menú")
        String description,
    @Schema(description = "Whether category is enabled", example = "true") boolean enabled) {

  /**
   * Creates a response from a category domain object.
   *
   * @param category the category
   * @return the response DTO
   */
  public static CategoryResponse fromDomain(Category category) {
    if (category == null) return null;
    return new CategoryResponse(
        category.getId(), category.getName(), category.getDescription(), category.isEnabled());
  }
}
