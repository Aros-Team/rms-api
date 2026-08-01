/* (C) 2026 */

package aros.services.rms.infraestructure.category.api.dto;

import aros.services.rms.core.category.domain.OptionCategory;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for option category data. */
@Schema(description = "Response DTO for option category data")
public record OptionCategoryResponse(
    @Schema(description = "Option category ID", example = "1") Long id,
    @Schema(description = "Category name", example = "Sizes") String name,
    @Schema(description = "Category description", example = "Available sizes for drinks")
        String description,
    @Schema(description = "Selection mode", example = "SINGLE_CHOICE") String selectionType) {

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
    return fromDomain(optionCategory, "SINGLE_CHOICE");
  }

  /**
   * Creates a response with the selection type loaded by the native read projection.
   *
   * @param optionCategory the option category
   * @param selectionType projected selection type
   * @return the response DTO
   */
  public static OptionCategoryResponse fromDomain(
      OptionCategory optionCategory, String selectionType) {
    if (optionCategory == null) {
      return null;
    }
    return new OptionCategoryResponse(
        optionCategory.getId(),
        optionCategory.getName(),
        optionCategory.getDescription(),
        selectionType == null || selectionType.isBlank() ? "SINGLE_CHOICE" : selectionType);
  }
}
