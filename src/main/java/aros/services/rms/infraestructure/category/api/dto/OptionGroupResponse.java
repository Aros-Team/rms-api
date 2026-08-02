/* (C) 2026 */

package aros.services.rms.infraestructure.category.api.dto;

import aros.services.rms.core.category.domain.OptionGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Response DTO for option group data. */
@Schema(description = "Response DTO for option group data")
public record OptionGroupResponse(
    @Schema(description = "Option group ID", example = "1") Long id,
    @Schema(description = "Group name", example = "Sizes") String name,
    @Schema(description = "Group description", example = "Available sizes for drinks")
        String description,
    @Schema(description = "Selection mode", example = "SINGLE_CHOICE") String selectionType,
    @Schema(description = "IDs of associated products") List<Long> productIds) {

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
    return fromDomain(optionGroup, "SINGLE_CHOICE", List.of());
  }

  /**
   * Creates a response with selection type and associated product IDs.
   *
   * @param optionGroup the option group
   * @param selectionType projected selection type
   * @param productIds associated product IDs
   * @return the response DTO
   */
  public static OptionGroupResponse fromDomain(
      OptionGroup optionGroup, String selectionType, List<Long> productIds) {
    if (optionGroup == null) {
      return null;
    }
    return new OptionGroupResponse(
        optionGroup.getId(),
        optionGroup.getName(),
        optionGroup.getDescription(),
        selectionType == null || selectionType.isBlank() ? "SINGLE_CHOICE" : selectionType,
        productIds == null ? List.of() : productIds);
  }
}
