/* (C) 2026 */

package aros.services.rms.core.product.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.payroll.domain.port.output.AreaLaborCostPort;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.product.domain.ProductCost;
import aros.services.rms.core.product.domain.ProductCost.CostBreakdownItem;
import aros.services.rms.core.product.port.input.CalculateProductCostUseCase;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Calculates the production cost of a product on-the-fly.
 *
 * <p>Material cost = sum of (recipe item quantity * supply variant unit cost). Labor cost =
 * (estimated prep minutes / 60) * average hourly rate of active workers in the product's
 * preparation area. Hourly rate = monthly salary / 160.
 */
@RequiredArgsConstructor
public class CalculateProductCostService implements CalculateProductCostUseCase {

  private static final BigDecimal SIXTY = BigDecimal.valueOf(60);
  private static final int COST_SCALE = 2;
  private static final int CALC_SCALE = 6;
  private static final String TYPE_MATERIAL = "MATERIAL";
  private static final String TYPE_LABOR = "LABOR";

  private final ProductRepositoryPort productRepositoryPort;
  private final ProductRecipeRepositoryPort productRecipeRepositoryPort;
  private final SupplyVariantRepositoryPort supplyVariantRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;
  private final AreaLaborCostPort areaLaborCostPort;
  private final Logger logger;

  /** {@inheritDoc} */
  @Override
  public ProductCost calculateCost(Long productId) {
    return calculateCost(productId, null);
  }

  /** {@inheritDoc} */
  @Override
  public ProductCost calculateCost(Long productId, YearMonth period) {
    var product =
        productRepositoryPort
            .findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

    BigDecimal materialCost = BigDecimal.ZERO;
    List<CostBreakdownItem> breakdown = new ArrayList<>();

    var recipes = productRecipeRepositoryPort.findByProductId(productId);
    if (recipes != null && !recipes.isEmpty()) {
      List<Long> variantIds = recipes.stream().map(r -> r.getSupplyVariantId()).distinct().toList();
      var variants = supplyVariantRepositoryPort.findAllById(variantIds);
      Map<Long, BigDecimal> variantMap =
          variants.stream()
              .collect(
                  Collectors.toMap(
                      v -> v.getId(),
                      v -> v.getUnitCost() != null ? v.getUnitCost().amount() : BigDecimal.ZERO,
                      (a, b) -> a));

      for (var recipe : recipes) {
        BigDecimal unitCost = variantMap.get(recipe.getSupplyVariantId());
        if (unitCost != null && recipe.getRequiredQuantity() != null) {
          BigDecimal lineCost = recipe.getRequiredQuantity().multiply(unitCost);
          materialCost = materialCost.add(lineCost);
          breakdown.add(
              new CostBreakdownItem(
                  "Material: variant " + recipe.getSupplyVariantId(),
                  lineCost.setScale(COST_SCALE, RoundingMode.HALF_UP),
                  TYPE_MATERIAL));
        } else {
          breakdown.add(
              new CostBreakdownItem(
                  "Material: variant " + recipe.getSupplyVariantId() + " (no unit cost)",
                  BigDecimal.ZERO,
                  TYPE_MATERIAL));
        }
      }
    }

    BigDecimal laborCost = BigDecimal.ZERO;
    if (product.getEstimatedPrepMinutes() != null && product.getEstimatedPrepMinutes() > 0) {
      if (product.getPreparationAreaId() != null) {
        YearMonth effectivePeriod = period != null ? period : YearMonth.now();
        Money costPerHour =
            areaLaborCostPort.calculateCostPerHour(product.getPreparationAreaId(), effectivePeriod);

        if (costPerHour.isPositive()) {
          BigDecimal hours =
              BigDecimal.valueOf(product.getEstimatedPrepMinutes())
                  .divide(SIXTY, CALC_SCALE, RoundingMode.HALF_UP);
          laborCost = costPerHour.amount().multiply(hours);
          breakdown.add(
              new CostBreakdownItem(
                  "Labor: $"
                      + costPerHour.amount().setScale(2, RoundingMode.HALF_UP)
                      + "/h * "
                      + hours.setScale(2, RoundingMode.HALF_UP)
                      + "h (area "
                      + product.getPreparationAreaId()
                      + ", mode: "
                      + effectivePeriod
                      + ")",
                  laborCost.setScale(COST_SCALE, RoundingMode.HALF_UP),
                  TYPE_LABOR));
        } else {
          breakdown.add(
              new CostBreakdownItem(
                  "Labor: no cost data for area " + product.getPreparationAreaId(),
                  BigDecimal.ZERO,
                  TYPE_LABOR));
        }
      }
    }

    BigDecimal totalCost = materialCost.add(laborCost);
    logger.info(
        "Calculated cost for product {}: total=${} material=${} labor=${}",
        productId,
        totalCost,
        materialCost,
        laborCost);

    return new ProductCost(
        productId,
        totalCost.setScale(COST_SCALE, RoundingMode.HALF_UP),
        materialCost.setScale(COST_SCALE, RoundingMode.HALF_UP),
        laborCost.setScale(COST_SCALE, RoundingMode.HALF_UP),
        breakdown);
  }
}
