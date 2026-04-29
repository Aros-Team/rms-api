/* (C) 2026 */

package aros.services.rms.infraestructure.category.api.dto;

import aros.services.rms.core.category.domain.OptionCategory;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for option category data. */
@Schema(description = "Response DTO para datos de categoría de opción")
public record OptionCategoryResponse(
    @Schema(description = "ID de la categoría de opción", example = "1") Long id,
    @Schema(description = "Nombre de la categoría", example = "Tamaños") String name,
    @Schema(
            description = "Descripción de la categoría",
            example = "Tamaños disponibles para bebidas")
        String description) {

  /**
   * Creates a response from an option category domain object.
   *
   * @param optionCategory the option category
   * @return the response DTO
   */
  public static OptionCategoryResponse fromDomain(OptionCategory optionCategory) {
    if (optionCategory == null) {
      return null;
    }
    return new OptionCategoryResponse(
        optionCategory.getId(), optionCategory.getName(), optionCategory.getDescription());
  }
}
