/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.mapper;

import aros.services.rms.core.product.domain.ProductCostBreakdown;
import aros.services.rms.infraestructure.product.api.dto.ProductCostBreakdownResponse;
import aros.services.rms.infraestructure.product.api.dto.ProductCostBreakdownResponse.CategoryCostResponse;
import aros.services.rms.infraestructure.product.api.dto.ProductCostBreakdownResponse.OptionCostResponse;
import org.springframework.stereotype.Component;

/** Maps product cost-breakdown domain projections to REST responses. */
@Component
public class ProductCostBreakdownResponseMapper {

  /**
   * Maps a product cost breakdown to its REST response.
   *
   * @param breakdown domain cost breakdown
   * @return REST response, or null when the source is null
   */
  public ProductCostBreakdownResponse toResponse(ProductCostBreakdown breakdown) {
    if (breakdown == null) {
      return null;
    }
    return new ProductCostBreakdownResponse(
        breakdown.productId(),
        breakdown.name(),
        breakdown.baseCost(),
        breakdown.options().stream()
            .map(
                option ->
                    new OptionCostResponse(
                        option.optionId(),
                        option.name(),
                        option.cost(),
                        option.extraPrice(),
                        option.categoryId(),
                        option.categorySelectionType()))
            .toList(),
        breakdown.categories().stream()
            .map(
                category ->
                    new CategoryCostResponse(
                        category.categoryId(),
                        category.name(),
                        category.selectionType(),
                        category.defaultSlotCost(),
                        category.slotProjectedCost(),
                        category.projectedContribution()))
            .toList(),
        breakdown.projectedOptionCost(),
        breakdown.projectedEffectiveCost());
  }
}
