/* (C) 2026 */

package aros.services.rms.core.product.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.util.List;

/** Material-cost projection for a product and its customization options. */
public record ProductCostBreakdown(
    Long productId,
    String name,
    Money baseCost,
    List<OptionCost> options,
    List<CategoryCost> categories,
    Money projectedOptionCost,
    Money projectedEffectiveCost) {

  /** Material cost and configured surcharge for one product option. */
  public record OptionCost(
      Long optionId,
      String name,
      Money cost,
      Money extraPrice,
      Long categoryId,
      String categoryName,
      String categorySelectionType) {}

  /** Projected material-cost contribution for one option category. */
  public record CategoryCost(
      Long categoryId,
      String name,
      String selectionType,
      Money defaultSlotCost,
      Money slotProjectedCost,
      Money projectedContribution) {}
}
