package aros.services.rms.core.specialselection.application.service;

import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.inventory.domain.SupplyVariant;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.specialselection.application.exception.SupplyVariantUnitCostMissingException;
import aros.services.rms.core.specialselection.domain.SpecialSelectionAddition;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SuggestedPrice;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computes the unit price of a special selection and suggests a price based on recipe costs and the
 * requested margin percentage.
 */
public class SpecialSelectionPricingService {

  private final ProductRecipeRepositoryPort productRecipeRepositoryPort;
  private final SupplyVariantRepositoryPort supplyVariantRepositoryPort;

  /**
   * Creates a new special selection pricing service.
   *
   * @param productRecipeRepositoryPort the product recipe repository port
   * @param supplyVariantRepositoryPort the supply variant repository port
   */
  public SpecialSelectionPricingService(
      ProductRecipeRepositoryPort productRecipeRepositoryPort,
      SupplyVariantRepositoryPort supplyVariantRepositoryPort) {
    this.productRecipeRepositoryPort = productRecipeRepositoryPort;
    this.supplyVariantRepositoryPort = supplyVariantRepositoryPort;
  }

  /**
   * Computes the unit price by adding the configured additions to the base price.
   *
   * @param config the special selection configuration
   * @param additionIds the identifiers of selected additions
   * @return the computed unit price
   */
  public Double computeUnitPrice(SpecialSelectionConfiguration config, List<Long> additionIds) {
    double total = config.getBasePrice() != null ? config.getBasePrice() : 0.0;
    if (additionIds != null && config.getAdditions() != null) {
      Map<Long, SpecialSelectionAddition> additionMap =
          config.getAdditions().stream()
              .collect(Collectors.toMap(SpecialSelectionAddition::getId, a -> a));
      for (Long id : additionIds) {
        SpecialSelectionAddition add = additionMap.get(id);
        if (add != null && add.getExtraPrice() != null) {
          total += add.getExtraPrice();
        }
      }
    }
    return total;
  }

  /**
   * Suggests a price for the special selection based on recipe costs and the provided margin.
   *
   * @param config the special selection configuration
   * @param marginPercent the target margin percentage
   * @return the suggested price with cost breakdown
   */
  public SuggestedPrice suggestPrice(
      SpecialSelectionConfiguration config, BigDecimal marginPercent) {
    Set<Long> allProductIds =
        config.getGroups().stream()
            .filter(g -> g.getProductIds() != null)
            .flatMap(g -> g.getProductIds().stream())
            .collect(Collectors.toSet());

    List<ProductRecipe> allProductRecipes = new ArrayList<>();
    for (Long pid : allProductIds) {
      allProductRecipes.addAll(productRecipeRepositoryPort.findByProductId(pid));
    }

    Map<Long, BigDecimal> variantCost = loadVariantCosts(allProductRecipes);

    List<Long> missingVariantIds =
        allProductRecipes.stream()
            .map(ProductRecipe::getSupplyVariantId)
            .filter(
                vid -> {
                  BigDecimal cost = variantCost.get(vid);
                  return cost == null || cost.compareTo(BigDecimal.ZERO) == 0;
                })
            .distinct()
            .toList();

    if (!missingVariantIds.isEmpty()) {
      throw new SupplyVariantUnitCostMissingException(missingVariantIds);
    }

    BigDecimal totalCost = BigDecimal.ZERO;
    List<SuggestedPrice.CostBreakdownItem> breakdown = new ArrayList<>();

    for (ProductRecipe recipe : allProductRecipes) {
      BigDecimal unitCost = variantCost.get(recipe.getSupplyVariantId());
      if (unitCost != null) {
        BigDecimal lineCost = recipe.getRequiredQuantity().multiply(unitCost);
        totalCost = totalCost.add(lineCost);
        breakdown.add(
            SuggestedPrice.CostBreakdownItem.builder()
                .optionId(null)
                .name("product:" + recipe.getProductId())
                .cost(lineCost)
                .build());
      }
    }

    if (config.isBaseRecipeEnabled()) {
      List<ProductRecipe> baseRecipes =
          productRecipeRepositoryPort.findByProductId(config.getProductId());
      for (ProductRecipe recipe : baseRecipes) {
        BigDecimal unitCost = variantCost.get(recipe.getSupplyVariantId());
        if (unitCost != null) {
          BigDecimal lineCost = recipe.getRequiredQuantity().multiply(unitCost);
          totalCost = totalCost.add(lineCost);
          breakdown.add(
              SuggestedPrice.CostBreakdownItem.builder()
                  .optionId(null)
                  .name("base:" + recipe.getSupplyVariantId())
                  .cost(lineCost)
                  .build());
        }
      }
    }

    BigDecimal marginFactor =
        marginPercent.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
    BigDecimal suggestedPrice =
        totalCost.divide(BigDecimal.ONE.subtract(marginFactor), 2, java.math.RoundingMode.HALF_UP);

    return SuggestedPrice.builder()
        .suggestedPrice(suggestedPrice)
        .totalCost(totalCost)
        .marginPercent(marginPercent)
        .breakdown(breakdown)
        .hasUnitCosts(true)
        .build();
  }

  private Map<Long, BigDecimal> loadVariantCosts(List<ProductRecipe> recipes) {
    Set<Long> variantIds =
        recipes.stream().map(ProductRecipe::getSupplyVariantId).collect(Collectors.toSet());
    List<SupplyVariant> variants =
        supplyVariantRepositoryPort.findAllById(new ArrayList<>(variantIds));
    return variants.stream()
        .collect(
            Collectors.toMap(
                SupplyVariant::getId,
                v -> v.getUnitCost() != null ? v.getUnitCost() : BigDecimal.ZERO));
  }
}
