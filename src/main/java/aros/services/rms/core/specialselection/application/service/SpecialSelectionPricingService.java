package aros.services.rms.core.specialselection.application.service;

import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.inventory.domain.SupplyVariant;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.specialselection.application.exception.SupplyVariantUnitCostMissingException;
import aros.services.rms.core.specialselection.domain.SpecialSelectionAddition;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SuggestedPrice;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computes the unit price of a special selection and suggests a price based on recipe costs and the
 * requested margin percentage.
 */
public class SpecialSelectionPricingService {

  private final ProductRecipeRepositoryPort productRecipeRepositoryPort;
  private final SupplyVariantRepositoryPort supplyVariantRepositoryPort;
  private final ProductRepositoryPort productRepositoryPort;

  /**
   * Creates a new special selection pricing service.
   *
   * @param productRecipeRepositoryPort the product recipe repository port
   * @param supplyVariantRepositoryPort the supply variant repository port
   * @param productRepositoryPort the product repository port
   */
  public SpecialSelectionPricingService(
      ProductRecipeRepositoryPort productRecipeRepositoryPort,
      SupplyVariantRepositoryPort supplyVariantRepositoryPort,
      ProductRepositoryPort productRepositoryPort) {
    this.productRecipeRepositoryPort = productRecipeRepositoryPort;
    this.supplyVariantRepositoryPort = supplyVariantRepositoryPort;
    this.productRepositoryPort = productRepositoryPort;
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

    // Collect all product IDs referenced in recipes and resolve names
    Set<Long> referencedProductIds =
        allProductRecipes.stream().map(ProductRecipe::getProductId).collect(Collectors.toSet());
    if (config.isBaseRecipeEnabled()) {
      referencedProductIds.add(config.getProductId());
    }
    Map<Long, Product> productById = loadProducts(referencedProductIds);

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
        String productName =
            productById.containsKey(recipe.getProductId())
                ? productById.get(recipe.getProductId()).getName()
                : "product:" + recipe.getProductId();
        breakdown.add(
            SuggestedPrice.CostBreakdownItem.builder()
                .optionId(null)
                .productId(recipe.getProductId())
                .name(productName)
                .cost(lineCost)
                .build());
      }
    }

    if (config.isBaseRecipeEnabled()) {
      List<ProductRecipe> baseRecipes =
          productRecipeRepositoryPort.findByProductId(config.getProductId());
      String baseProductName =
          productById.containsKey(config.getProductId())
              ? productById.get(config.getProductId()).getName()
              : "base:" + config.getProductId();
      for (ProductRecipe recipe : baseRecipes) {
        BigDecimal unitCost = variantCost.get(recipe.getSupplyVariantId());
        if (unitCost != null) {
          BigDecimal lineCost = recipe.getRequiredQuantity().multiply(unitCost);
          totalCost = totalCost.add(lineCost);
          breakdown.add(
              SuggestedPrice.CostBreakdownItem.builder()
                  .optionId(null)
                  .productId(null)
                  .name(baseProductName + ":" + recipe.getSupplyVariantId())
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
                v -> v.getUnitCost() != null ? v.getUnitCost().amount() : BigDecimal.ZERO));
  }

  private Map<Long, Product> loadProducts(Set<Long> productIds) {
    Map<Long, Product> result = new HashMap<>();
    for (Long pid : productIds) {
      Optional<Product> product = productRepositoryPort.findById(pid);
      product.ifPresent(p -> result.put(pid, p));
    }
    return result;
  }
}
