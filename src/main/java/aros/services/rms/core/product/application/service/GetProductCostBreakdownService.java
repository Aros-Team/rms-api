/* (C) 2026 */

package aros.services.rms.core.product.application.service;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.inventory.domain.SupplyVariant;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductCostBreakdown;
import aros.services.rms.core.product.domain.ProductCostBreakdown.CategoryCost;
import aros.services.rms.core.product.domain.ProductCostBreakdown.OptionCost;
import aros.services.rms.core.product.domain.ProductOptionCostProfile;
import aros.services.rms.core.product.port.input.GetProductCostBreakdownUseCase;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Calculates material-cost projections for a product and its configured option categories. */
public class GetProductCostBreakdownService implements GetProductCostBreakdownUseCase {

  private static final Currency COP = Currency.getInstance("COP");
  private static final String SINGLE_CHOICE = "SINGLE_CHOICE";
  private static final String MULTI_CHOICE = "MULTI_CHOICE";
  private static final String ADD_ON = "ADD_ON";
  private static final String REMOVAL = "REMOVAL";
  private static final int CALCULATION_SCALE = 10;

  private final ProductRepositoryPort productRepositoryPort;
  private final ProductRecipeRepositoryPort productRecipeRepositoryPort;
  private final SupplyVariantRepositoryPort supplyVariantRepositoryPort;
  private final ProductOptionRepositoryPort productOptionRepositoryPort;
  private final OptionRecipeRepositoryPort optionRecipeRepositoryPort;

  /**
   * Creates a product cost-breakdown service.
   *
   * @param productRepositoryPort product persistence port
   * @param productRecipeRepositoryPort product recipe persistence port
   * @param supplyVariantRepositoryPort supply variant persistence port
   * @param productOptionRepositoryPort product option persistence port
   * @param optionRecipeRepositoryPort option recipe persistence port
   */
  public GetProductCostBreakdownService(
      ProductRepositoryPort productRepositoryPort,
      ProductRecipeRepositoryPort productRecipeRepositoryPort,
      SupplyVariantRepositoryPort supplyVariantRepositoryPort,
      ProductOptionRepositoryPort productOptionRepositoryPort,
      OptionRecipeRepositoryPort optionRecipeRepositoryPort) {
    this.productRepositoryPort = productRepositoryPort;
    this.productRecipeRepositoryPort = productRecipeRepositoryPort;
    this.supplyVariantRepositoryPort = supplyVariantRepositoryPort;
    this.productOptionRepositoryPort = productOptionRepositoryPort;
    this.optionRecipeRepositoryPort = optionRecipeRepositoryPort;
  }

  /** {@inheritDoc} */
  @Override
  public ProductCostBreakdown execute(Long productId) {
    Product product =
        productRepositoryPort
            .findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

    Money baseCost = calculateBaseMaterialCost(productId);
    List<ProductOptionCostProfile> profiles =
        productOptionRepositoryPort.loadCostProfilesByProductId(productId);
    if (profiles == null) {
      profiles = List.of();
    }

    List<Long> optionIds =
        profiles.stream().map(ProductOptionCostProfile::optionId).distinct().toList();
    Map<Long, Money> optionCosts =
        optionIds.isEmpty()
            ? Map.of()
            : optionRecipeRepositoryPort.loadMaterialCostByOptionIds(optionIds);
    if (optionCosts == null) {
      optionCosts = Map.of();
    }

    List<OptionCost> options = buildOptions(profiles, optionCosts);
    List<CategoryCost> categories = buildCategories(profiles, optionCosts);
    Money projectedOptionCost =
        categories.stream()
            .map(CategoryCost::projectedContribution)
            .reduce(Money.zero(COP), Money::plus);

    return new ProductCostBreakdown(
        productId,
        product.getName(),
        baseCost,
        options,
        categories,
        projectedOptionCost,
        baseCost.plus(projectedOptionCost));
  }

  private Money calculateBaseMaterialCost(Long productId) {
    List<ProductRecipe> recipes = productRecipeRepositoryPort.findByProductId(productId);
    if (recipes == null || recipes.isEmpty()) {
      return Money.zero(COP);
    }

    List<Long> variantIds =
        recipes.stream().map(ProductRecipe::getSupplyVariantId).distinct().toList();
    Map<Long, Money> unitCosts = new LinkedHashMap<>();
    for (SupplyVariant variant : supplyVariantRepositoryPort.findAllById(variantIds)) {
      unitCosts.put(variant.getId(), moneyOrZero(variant.getUnitCost()));
    }

    BigDecimal total = BigDecimal.ZERO;
    for (ProductRecipe recipe : recipes) {
      Money unitCost = unitCosts.get(recipe.getSupplyVariantId());
      if (unitCost != null && recipe.getRequiredQuantity() != null) {
        total = total.add(unitCost.amount().multiply(recipe.getRequiredQuantity()));
      }
    }
    return new Money(total, COP);
  }

  private List<OptionCost> buildOptions(
      List<ProductOptionCostProfile> profiles, Map<Long, Money> optionCosts) {
    return profiles.stream()
        .map(
            profile ->
                new OptionCost(
                    profile.optionId(),
                    profile.optionName(),
                    moneyOrZero(optionCosts.get(profile.optionId())),
                    moneyOrZero(profile.extraPrice()),
                    profile.categoryId(),
                    profile.categoryName(),
                    normalizeSelectionType(profile.categorySelectionType())))
        .toList();
  }

  private List<CategoryCost> buildCategories(
      List<ProductOptionCostProfile> profiles, Map<Long, Money> optionCosts) {
    Map<Long, List<ProductOptionCostProfile>> profilesByCategory = new LinkedHashMap<>();
    for (ProductOptionCostProfile profile : profiles) {
      profilesByCategory
          .computeIfAbsent(profile.categoryId(), ignored -> new ArrayList<>())
          .add(profile);
    }

    List<CategoryCost> categories = new ArrayList<>();
    for (List<ProductOptionCostProfile> categoryProfiles : profilesByCategory.values()) {
      ProductOptionCostProfile first = categoryProfiles.getFirst();
      String selectionType = normalizeSelectionType(first.categorySelectionType());
      Money defaultSlotCost = moneyOrZero(first.defaultSlotCost());
      List<Money> costs =
          categoryProfiles.stream()
              .map(profile -> moneyOrZero(optionCosts.get(profile.optionId())))
              .toList();

      Projection projection =
          calculateProjection(
              selectionType, first.replaceSupplyCategoryId(), defaultSlotCost, costs);
      categories.add(
          new CategoryCost(
              first.categoryId(),
              first.categoryName(),
              selectionType,
              defaultSlotCost,
              projection.slotProjectedCost(),
              projection.contribution()));
    }
    return List.copyOf(categories);
  }

  private Projection calculateProjection(
      String selectionType,
      Long replaceSupplyCategoryId,
      Money defaultSlotCost,
      List<Money> optionCosts) {
    if (ADD_ON.equals(selectionType) || REMOVAL.equals(selectionType)) {
      return new Projection(Money.zero(COP), Money.zero(COP));
    }
    if (optionCosts.isEmpty()) {
      return new Projection(Money.zero(COP), Money.zero(COP));
    }

    Money optionTotal = optionCosts.stream().reduce(Money.zero(COP), Money::plus);
    if (SINGLE_CHOICE.equals(selectionType) && replaceSupplyCategoryId != null) {
      Money slotProjectedCost =
          defaultSlotCost
              .plus(optionTotal)
              .divide(
                  BigDecimal.valueOf(optionCosts.size() + 1L),
                  CALCULATION_SCALE,
                  RoundingMode.HALF_UP);
      return new Projection(slotProjectedCost, slotProjectedCost.minus(defaultSlotCost));
    }

    if (SINGLE_CHOICE.equals(selectionType) || MULTI_CHOICE.equals(selectionType)) {
      Money average =
          optionTotal.divide(
              BigDecimal.valueOf(optionCosts.size()), CALCULATION_SCALE, RoundingMode.HALF_UP);
      return new Projection(average, average);
    }
    return new Projection(Money.zero(COP), Money.zero(COP));
  }

  private String normalizeSelectionType(String selectionType) {
    if (selectionType == null || selectionType.isBlank()) {
      return SINGLE_CHOICE;
    }
    String normalized = selectionType.toUpperCase(Locale.ROOT);
    return switch (normalized) {
      case SINGLE_CHOICE, MULTI_CHOICE, ADD_ON, REMOVAL -> normalized;
      default -> SINGLE_CHOICE;
    };
  }

  private Money moneyOrZero(Money money) {
    return money == null ? Money.zero(COP) : money;
  }

  private record Projection(Money slotProjectedCost, Money contribution) {}
}
