/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.core.product.domain.ProductCostBreakdown.OptionCost;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.infraestructure.common.dto.MoneyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/** Response DTO for product option data. */
@Schema(description = "Response DTO for product option data")
public record ProductOptionResponse(
    @Schema(description = "Option ID", example = "1") Long id,
    @Schema(description = "Option name", example = "Large (1.5L)") String name,
    @Schema(description = "Option category ID", example = "1") Long optionGroupId,
    @Schema(description = "Option category name", example = "Sizes") String optionGroupName,
    @Schema(description = "Pure material cost of the option") MoneyDto cost,
    @Schema(description = "Configured surcharge when associated with the product")
        MoneyDto extraPrice,
    @Schema(description = "Selection mode of the option category", example = "SINGLE_CHOICE")
        String categorySelectionType,
    @Schema(description = "Option ingredient recipe") List<RecipeItemResponse> recipe) {

  /**
   * Creates a response from a domain object.
   *
   * @param option the product option
   * @return the response DTO
   */
  public static ProductOptionResponse fromDomain(ProductOption option) {
    if (option == null) {
      return null;
    }
    return new ProductOptionResponse(
        option.getId(),
        option.getName(),
        option.getCategory() != null ? option.getCategory().getId() : null,
        option.getCategory() != null ? option.getCategory().getName() : null,
        MoneyDto.of(BigDecimal.ZERO),
        MoneyDto.of(BigDecimal.ZERO),
        "SINGLE_CHOICE",
        RecipeItemResponse.fromDomain(option.getRecipe()));
  }

  /**
   * Creates a response from a domain object, projecting the {@code categorySelectionType} from the
   * option's category. Use this when the option is being returned in a group context (no
   * per-product cost/extraPrice yet known).
   *
   * @param option the product option
   * @return the response DTO with category-level selection type
   */
  public static ProductOptionResponse fromDomainInGroupContext(ProductOption option) {
    if (option == null) {
      return null;
    }
    String selectionType =
        option.getCategory() != null && option.getCategory().getSelectionType() != null
            ? option.getCategory().getSelectionType().name()
            : "SINGLE_CHOICE";
    return new ProductOptionResponse(
        option.getId(),
        option.getName(),
        option.getCategory() != null ? option.getCategory().getId() : null,
        option.getCategory() != null ? option.getCategory().getName() : null,
        MoneyDto.of(BigDecimal.ZERO),
        MoneyDto.of(BigDecimal.ZERO),
        selectionType,
        RecipeItemResponse.fromDomain(option.getRecipe()));
  }

  /**
   * Creates an enriched response from a product cost-breakdown option.
   *
   * @param option the projected option
   * @return the enriched response DTO
   */
  public static ProductOptionResponse fromCostBreakdown(OptionCost option) {
    if (option == null) {
      return null;
    }
    return new ProductOptionResponse(
        option.optionId(),
        option.name(),
        option.categoryId(),
        option.categoryName(),
        MoneyDto.of(option.cost()),
        MoneyDto.of(option.extraPrice()),
        option.categorySelectionType(),
        List.of());
  }
}
