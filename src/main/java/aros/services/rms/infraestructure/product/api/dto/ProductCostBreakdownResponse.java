/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.core.common.money.domain.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Response DTO for a product material-cost and option projection breakdown. */
@Schema(description = "Product material-cost projection including customization options")
public record ProductCostBreakdownResponse(
    @Schema(description = "Product ID", example = "1") Long productId,
    @Schema(description = "Product name", example = "Hamburguesa Clásica") String name,
    @Schema(description = "Base recipe material cost") Money baseCost,
    @Schema(description = "Associated options and their material costs")
        List<OptionCostResponse> options,
    @Schema(description = "Option-category projected contributions")
        List<CategoryCostResponse> categories,
    @Schema(description = "Sum of projected option-category contributions")
        Money projectedOptionCost,
    @Schema(description = "Base cost plus projected option cost") Money projectedEffectiveCost) {

  /** One associated option with material cost and configured surcharge. */
  @Schema(description = "Associated product option cost")
  public record OptionCostResponse(
      @Schema(description = "Option ID", example = "3") Long optionId,
      @Schema(description = "Option name", example = "Pollo") String name,
      @Schema(description = "Pure option material cost") Money cost,
      @Schema(description = "Configured product surcharge") Money extraPrice,
      @Schema(description = "Option category ID", example = "2") Long categoryId,
      @Schema(description = "Category selection mode", example = "SINGLE_CHOICE")
          String categorySelectionType) {}

  /** One option category and its projected material-cost contribution. */
  @Schema(description = "Projected option-category cost")
  public record CategoryCostResponse(
      @Schema(description = "Option category ID", example = "2") Long categoryId,
      @Schema(description = "Option category name", example = "Proteína") String name,
      @Schema(description = "Category selection mode", example = "SINGLE_CHOICE")
          String selectionType,
      @Schema(description = "Base-recipe material cost replaced by this category")
          Money defaultSlotCost,
      @Schema(description = "Projected material cost for this category slot")
          Money slotProjectedCost,
      @Schema(description = "Projected category contribution over the base recipe")
          Money projectedContribution) {}
}
