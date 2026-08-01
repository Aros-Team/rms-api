/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.product.domain.ProductCostBreakdown.OptionCost;
import aros.services.rms.core.product.domain.ProductOption;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Currency;

/** Response DTO for product option data. */
@Schema(description = "Response DTO for product option data")
public record ProductOptionResponse(
    @Schema(description = "Option ID", example = "1") Long id,
    @Schema(description = "Option name", example = "Large (1.5L)") String name,
    @Schema(description = "Option category ID", example = "1") Long optionCategoryId,
    @Schema(description = "Option category name", example = "Sizes") String optionCategoryName,
    @Schema(description = "Pure material cost of the option") Money cost,
    @Schema(description = "Configured surcharge when associated with the product") Money extraPrice,
    @Schema(description = "Selection mode of the option category", example = "SINGLE_CHOICE")
        String categorySelectionType) {

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
        Money.zero(Currency.getInstance("COP")),
        Money.zero(Currency.getInstance("COP")),
        "SINGLE_CHOICE");
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
        option.cost(),
        option.extraPrice(),
        option.categorySelectionType());
  }
}
