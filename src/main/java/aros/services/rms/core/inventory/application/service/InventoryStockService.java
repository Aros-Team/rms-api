/* (C) 2026 */

package aros.services.rms.core.inventory.application.service;

import aros.services.rms.core.category.domain.OptionGroup;
import aros.services.rms.core.category.domain.OptionSelectionType;
import aros.services.rms.core.inventory.application.exception.StorageLocationNotFoundException;
import aros.services.rms.core.inventory.domain.InventoryStock;
import aros.services.rms.core.inventory.domain.OptionRecipe;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of inventory stock availability checks.
 *
 * <p>Phase D — semantics: applies the selection-mode semantics when consolidating the required
 * supply-variant map:
 *
 * <ul>
 *   <li>{@code SINGLE_CHOICE} with {@code replace_supply_category_id} (substitution slot): the
 *       base-recipe lines of the slot are subtracted and the selected option's recipe is added
 *       instead.
 *   <li>{@code REMOVE}: the selected option's recipe is subtracted from the base.
 *   <li>{@code SINGLE_CHOICE} (no replacement), {@code MULTI_CHOICE}, {@code EXTRA}: selected
 *       options' recipes are added (current behavior).
 *   <li>Categories with no selection: the base recipe stays intact.
 * </ul>
 */
@RequiredArgsConstructor
public class InventoryStockService implements InventoryStockUseCase {

  private final ProductRecipeRepositoryPort productRecipeRepositoryPort;
  private final OptionRecipeRepositoryPort optionRecipeRepositoryPort;
  private final InventoryStockRepositoryPort inventoryStockRepositoryPort;
  private final StorageLocationRepositoryPort storageLocationRepositoryPort;
  private final ProductOptionRepositoryPort productOptionRepositoryPort;

  @Override
  public boolean isAvailable(Long productId, List<Long> selectedOptionIds) {
    Map<Long, BigDecimal> requiredVariants =
        computeRequiredVariantsForSingleProduct(productId, selectedOptionIds);

    if (requiredVariants.isEmpty()) {
      return true;
    }

    // Get storage location IDs - both locations must exist for inventory checks to work
    Long cocinaId = getStorageLocationId("Cocina");
    Long bodegaId =
        getStorageLocationId("Bodega"); // throws StorageLocationNotFoundException if missing

    // Check availability for each required variant
    for (Map.Entry<Long, BigDecimal> entry : requiredVariants.entrySet()) {
      Long variantId = entry.getKey();
      BigDecimal required = entry.getValue();
      // Negative values after substitution/remove mean net outflow is lower; clamp to zero for the
      // stock check (we still need to ensure we don't deduct more than required).
      if (required.signum() <= 0) {
        continue;
      }

      // Check Cocina first
      BigDecimal cocinaStock = getStockQuantity(variantId, cocinaId);
      if (cocinaStock.compareTo(required) >= 0) {
        continue; // Sufficient stock in Cocina
      }

      // Check Bodega as fallback
      BigDecimal bodegaStock = getStockQuantity(variantId, bodegaId);
      BigDecimal totalAvailable = cocinaStock.add(bodegaStock);
      if (totalAvailable.compareTo(required) < 0) {
        return false; // Not enough stock in either location
      }
    }

    return true;
  }

  /**
   * Computes the consolidated required supply-variant map for a single product, applying
   * selection-mode semantics over the selected options.
   *
   * @param productId the product identifier
   * @param selectedOptionIds the list of selected option ids (may be null or empty)
   * @return variant id → net required quantity; entries are non-null but may be non-positive after
   *     substitution/remove (the caller should treat negatives as zero for stock checks)
   */
  private Map<Long, BigDecimal> computeRequiredVariantsForSingleProduct(
      Long productId, List<Long> selectedOptionIds) {
    Map<Long, BigDecimal> required = new LinkedHashMap<>();

    // Base recipe contributes the starting balance.
    List<ProductRecipe> productRecipes = productRecipeRepositoryPort.findByProductId(productId);
    if (productRecipes != null) {
      for (ProductRecipe recipe : productRecipes) {
        required.merge(recipe.getSupplyVariantId(), recipe.getRequiredQuantity(), BigDecimal::add);
      }
    }

    if (selectedOptionIds == null || selectedOptionIds.isEmpty()) {
      return required;
    }

    List<ProductOption> selectedOptions =
        productOptionRepositoryPort.findAllById(selectedOptionIds);
    applySelectionModeSemantics(productId, selectedOptions, required);

    return required;
  }

  /**
   * Applies the selection-mode semantics to the required-variants map. See the class-level Javadoc
   * for the full rule set. The map is mutated in place.
   *
   * @param productId the product identifier (used for slot lookups)
   * @param selectedOptions the selected product options with categories populated
   * @param required the consolidated map to mutate
   */
  private void applySelectionModeSemantics(
      Long productId, List<ProductOption> selectedOptions, Map<Long, BigDecimal> required) {
    if (selectedOptions == null || selectedOptions.isEmpty()) {
      return;
    }
    // Group by category id; ignore options without a category (defensive).
    Map<Long, List<ProductOption>> byCategory =
        selectedOptions.stream()
            .filter(
                opt ->
                    opt != null && opt.getCategory() != null && opt.getCategory().getId() != null)
            .collect(
                Collectors.groupingBy(
                    opt -> opt.getCategory().getId(), LinkedHashMap::new, Collectors.toList()));

    for (Map.Entry<Long, List<ProductOption>> entry : byCategory.entrySet()) {
      List<ProductOption> opts = entry.getValue();
      OptionGroup category = opts.get(0).getCategory();
      OptionSelectionType type =
          category.getSelectionType() == null
              ? OptionSelectionType.SINGLE_CHOICE
              : category.getSelectionType();

      if (type == OptionSelectionType.SINGLE_CHOICE
          && category.getReplaceSupplyCategoryId() != null
          && opts.size() == 1) {
        // Substitution: subtract the slot's base-recipe lines and add the option's recipe.
        List<ProductRecipe> slotRecipes =
            productOptionRepositoryPort.loadBaseRecipeBySupplyCategory(
                productId, category.getReplaceSupplyCategoryId());
        if (slotRecipes != null) {
          for (ProductRecipe slot : slotRecipes) {
            required.merge(
                slot.getSupplyVariantId(),
                slot.getRequiredQuantity() == null
                    ? BigDecimal.ZERO
                    : slot.getRequiredQuantity().negate(),
                BigDecimal::add);
          }
        }
        addOptionRecipes(List.of(opts.get(0)), required);
      } else if (type == OptionSelectionType.REMOVAL) {
        // Subtract the option's recipe(s) from the base.
        subtractOptionRecipes(opts, required);
      } else {
        // Default: add selected option recipes.
        addOptionRecipes(opts, required);
      }
    }
  }

  private void addOptionRecipes(List<ProductOption> options, Map<Long, BigDecimal> required) {
    if (options == null || options.isEmpty()) {
      return;
    }
    List<Long> optionIds = new ArrayList<>(options.size());
    for (ProductOption opt : options) {
      if (opt != null && opt.getId() != null) {
        optionIds.add(opt.getId());
      }
    }
    if (optionIds.isEmpty()) {
      return;
    }
    List<OptionRecipe> optionRecipes = optionRecipeRepositoryPort.findByOptionIdIn(optionIds);
    if (optionRecipes == null) {
      return;
    }
    for (OptionRecipe recipe : optionRecipes) {
      required.merge(
          recipe.getSupplyVariantId(),
          recipe.getRequiredQuantity() == null ? BigDecimal.ZERO : recipe.getRequiredQuantity(),
          BigDecimal::add);
    }
  }

  private void subtractOptionRecipes(List<ProductOption> options, Map<Long, BigDecimal> required) {
    if (options == null || options.isEmpty()) {
      return;
    }
    List<Long> optionIds = new ArrayList<>(options.size());
    for (ProductOption opt : options) {
      if (opt != null && opt.getId() != null) {
        optionIds.add(opt.getId());
      }
    }
    if (optionIds.isEmpty()) {
      return;
    }
    List<OptionRecipe> optionRecipes = optionRecipeRepositoryPort.findByOptionIdIn(optionIds);
    if (optionRecipes == null) {
      return;
    }
    for (OptionRecipe recipe : optionRecipes) {
      required.merge(
          recipe.getSupplyVariantId(),
          recipe.getRequiredQuantity() == null
              ? BigDecimal.ZERO
              : recipe.getRequiredQuantity().negate(),
          BigDecimal::add);
    }
  }

  private Long getStorageLocationId(String name) {
    return storageLocationRepositoryPort
        .findByName(name)
        .orElseThrow(() -> new StorageLocationNotFoundException(name))
        .getId();
  }

  private BigDecimal getStockQuantity(Long variantId, Long locationId) {
    return inventoryStockRepositoryPort
        .findByVariantAndLocationWithLock(variantId, locationId)
        .map(InventoryStock::getCurrentQuantity)
        .orElse(BigDecimal.ZERO);
  }
}
