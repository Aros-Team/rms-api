/* (C) 2026 */

package aros.services.rms.infraestructure.category.api.dto;

import aros.services.rms.core.category.domain.OptionGroup;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for option group data. */
@Schema(description = "Response DTO for option group data")
public record OptionGroupResponse(
    @Schema(description = "Option category ID", example = "1") Long id,
    @Schema(description = "Category name", example = "Sizes") String name,
    @Schema(description = "Category description", example = "Available sizes for drinks")
        String description,
    @Schema(description = "Selection mode", example = "SINGLE_CHOICE") String selectionType) {

  /**
   * Creates a response from an option group domain object.
   *
   * @param optionGroup the option group
   * @return the response DTO
   */
  public static OptionGroupResponse fromDomain(OptionGroup optionGroup) {
    if (optionGroup == null) {
      return null;
    }
    return fromDomain(optionGroup, "SINGLE_CHOICE");
  }

  /**
   * Creates a response with the selection type loaded by the native read projection.
   *
   * @param optionGroup the option group
   * @param selectionType projected selection type
   * @return the response DTO
   */
  public static OptionGroupResponse fromDomain(OptionGroup optionGroup, String selectionType) {
    if (optionGroup == null) {
      return null;
    }
    return new OptionGroupResponse(
        optionGroup.getId(),
        optionGroup.getName(),
        optionGroup.getDescription(),
        selectionType == null || selectionType.isBlank() ? "SINGLE_CHOICE" : selectionType);
  }
}
