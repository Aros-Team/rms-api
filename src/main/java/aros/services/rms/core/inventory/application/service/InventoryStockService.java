/* (C) 2026 */
package aros.services.rms.core.inventory.application.service;

import aros.services.rms.core.inventory.application.exception.StorageLocationNotFoundException;
import aros.services.rms.core.inventory.domain.InventoryStock;
import aros.services.rms.core.inventory.domain.OptionRecipe;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of inventory stock availability checks. */
@Service
@RequiredArgsConstructor
public class InventoryStockService implements InventoryStockUseCase {

  private final ProductRecipeRepositoryPort productRecipeRepositoryPort;
  private final OptionRecipeRepositoryPort optionRecipeRepositoryPort;
  private final InventoryStockRepositoryPort inventoryStockRepositoryPort;
  private final StorageLocationRepositoryPort storageLocationRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public boolean isAvailable(Long productId, List<Long> selectedOptionIds) {
    // Get product recipes
    List<ProductRecipe> productRecipes = productRecipeRepositoryPort.findByProductId(productId);

    // If no recipe, product is available
    if (productRecipes == null || productRecipes.isEmpty()) {
      return true;
    }

    // Consolidate all required supply variants and quantities
    Map<Long, BigDecimal> requiredVariants = new HashMap<>();

    // Add product recipe requirements
    for (ProductRecipe recipe : productRecipes) {
      requiredVariants.merge(
          recipe.getSupplyVariantId(), recipe.getRequiredQuantity(), BigDecimal::add);
    }

    // Add option recipe requirements if options are selected
    if (selectedOptionIds != null && !selectedOptionIds.isEmpty()) {
      List<OptionRecipe> optionRecipes =
          optionRecipeRepositoryPort.findByOptionIdIn(selectedOptionIds);
      for (OptionRecipe recipe : optionRecipes) {
        requiredVariants.merge(
            recipe.getSupplyVariantId(), recipe.getRequiredQuantity(), BigDecimal::add);
      }
    }

    // Get storage location IDs - both locations must exist for inventory checks to work
    Long cocinaId = getStorageLocationId("Cocina");
    Long bodegaId = getStorageLocationId("Bodega"); // throws StorageLocationNotFoundException if missing

    // Check availability for each required variant
    for (Map.Entry<Long, BigDecimal> entry : requiredVariants.entrySet()) {
      Long variantId = entry.getKey();
      BigDecimal required = entry.getValue();

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
