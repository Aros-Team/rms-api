package aros.services.rms.core.specialselection.application.service;

import aros.services.rms.core.inventory.domain.OptionRecipe;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.inventory.domain.SupplyVariant;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.product.domain.ProductOption;
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

  private final OptionRecipeRepositoryPort optionRecipeRepositoryPort;
  private final ProductRecipeRepositoryPort productRecipeRepositoryPort;
  private final SupplyVariantRepositoryPort supplyVariantRepositoryPort;

  /**
   * Creates a new special selection pricing service.
   *
   * @param optionRecipeRepositoryPort the option recipe repository port
   * @param productRecipeRepositoryPort the product recipe repository port
   * @param supplyVariantRepositoryPort the supply variant repository port
   */
  public SpecialSelectionPricingService(
      OptionRecipeRepositoryPort optionRecipeRepositoryPort,
      ProductRecipeRepositoryPort productRecipeRepositoryPort,
      SupplyVariantRepositoryPort supplyVariantRepositoryPort) {
    this.optionRecipeRepositoryPort = optionRecipeRepositoryPort;
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
    Set<Long> allOptionIds =
        config.getGroups().stream()
            .filter(g -> g.getOptions() != null)
            .flatMap(g -> g.getOptions().stream().map(ProductOption::getId))
            .collect(Collectors.toSet());

    Set<Long> additionOptionIds =
        config.getAdditions() != null
            ? config.getAdditions().stream()
                .map(SpecialSelectionAddition::getOptionId)
                .collect(Collectors.toSet())
            : Set.of();

    allOptionIds.addAll(additionOptionIds);

    List<OptionRecipe> allOptionRecipes =
        optionRecipeRepositoryPort.findByOptionIdIn(new ArrayList<>(allOptionIds));

    Map<Long, BigDecimal> variantCost = loadVariantCosts(allOptionRecipes);

    List<OptionRecipe> missingCostRecipes =
        allOptionRecipes.stream()
            .filter(
                r -> {
                  BigDecimal cost = variantCost.get(r.getSupplyVariantId());
                  return cost == null || cost.compareTo(BigDecimal.ZERO) == 0;
                })
            .toList();

    if (!missingCostRecipes.isEmpty()) {
      List<Long> missingVariantIds =
          missingCostRecipes.stream().map(OptionRecipe::getSupplyVariantId).distinct().toList();
      throw new SupplyVariantUnitCostMissingException(missingVariantIds);
    }

    BigDecimal totalCost = BigDecimal.ZERO;
    List<SuggestedPrice.CostBreakdownItem> breakdown = new ArrayList<>();

    for (Long optionId : allOptionIds) {
      BigDecimal optionCost = BigDecimal.ZERO;
      List<OptionRecipe> recipes =
          allOptionRecipes.stream().filter(r -> r.getOptionId().equals(optionId)).toList();
      for (OptionRecipe recipe : recipes) {
        BigDecimal unitCost = variantCost.get(recipe.getSupplyVariantId());
        if (unitCost != null) {
          BigDecimal lineCost = recipe.getRequiredQuantity().multiply(unitCost);
          optionCost = optionCost.add(lineCost);
        }
      }
      if (optionCost.compareTo(BigDecimal.ZERO) > 0) {
        totalCost = totalCost.add(optionCost);
        breakdown.add(
            SuggestedPrice.CostBreakdownItem.builder()
                .optionId(optionId)
                .name("option:" + optionId)
                .cost(optionCost)
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

  private Map<Long, BigDecimal> loadVariantCosts(List<OptionRecipe> recipes) {
    Set<Long> variantIds =
        recipes.stream().map(OptionRecipe::getSupplyVariantId).collect(Collectors.toSet());
    List<SupplyVariant> variants =
        supplyVariantRepositoryPort.findAllById(new ArrayList<>(variantIds));
    return variants.stream()
        .collect(
            Collectors.toMap(
                SupplyVariant::getId,
                v -> v.getUnitCost() != null ? v.getUnitCost() : BigDecimal.ZERO));
  }
}
