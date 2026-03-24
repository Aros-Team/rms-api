/* (C) 2026 */
package aros.services.rms.core.inventory.application.usecases;

import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.application.exception.StorageLocationNotFoundException;
import aros.services.rms.core.inventory.domain.InventoryMovement;
import aros.services.rms.core.inventory.domain.InventoryStock;
import aros.services.rms.core.inventory.domain.MovementType;
import aros.services.rms.core.inventory.domain.OptionRecipe;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.inventory.port.input.InventoryMovementUseCase;
import aros.services.rms.core.inventory.port.output.InventoryMovementRepositoryPort;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.product.domain.ProductOption;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of inventory movement operations. */
@Service
@RequiredArgsConstructor
public class InventoryMovementUseCaseImpl implements InventoryMovementUseCase {

  private final ProductRecipeRepositoryPort productRecipeRepositoryPort;
  private final OptionRecipeRepositoryPort optionRecipeRepositoryPort;
  private final InventoryStockRepositoryPort inventoryStockRepositoryPort;
  private final InventoryMovementRepositoryPort inventoryMovementRepositoryPort;
  private final StorageLocationRepositoryPort storageLocationRepositoryPort;

  @Override
  @Transactional
  public void deductForOrder(Long orderId, List<OrderDetail> details) {
    // Consolidate all required supply variants and quantities
    Map<Long, BigDecimal> requiredVariants = new HashMap<>();

    for (OrderDetail detail : details) {
      // Add product recipe requirements
      List<ProductRecipe> productRecipes =
          productRecipeRepositoryPort.findByProductId(detail.getProduct().getId());
      for (ProductRecipe recipe : productRecipes) {
        requiredVariants.merge(
            recipe.getSupplyVariantId(), recipe.getRequiredQuantity(), BigDecimal::add);
      }

      // Add option recipe requirements
      if (detail.getSelectedOptions() != null && !detail.getSelectedOptions().isEmpty()) {
        List<Long> optionIds =
            detail.getSelectedOptions().stream().map(ProductOption::getId).toList();
        List<OptionRecipe> optionRecipes = optionRecipeRepositoryPort.findByOptionIdIn(optionIds);
        for (OptionRecipe recipe : optionRecipes) {
          requiredVariants.merge(
              recipe.getSupplyVariantId(), recipe.getRequiredQuantity(), BigDecimal::add);
        }
      }
    }

    // Get storage location IDs
    Long cocinaId = getStorageLocationId("Cocina");
    Long bodegaId = getStorageLocationId("Bodega");

    // Deduct stock for each required variant
    for (Map.Entry<Long, BigDecimal> entry : requiredVariants.entrySet()) {
      Long variantId = entry.getKey();
      BigDecimal required = entry.getValue();

      // Try to deduct from Cocina first
      BigDecimal cocinaDeducted = deductFromLocation(variantId, cocinaId, required);

      // If Cocina doesn't have enough, deduct the rest from Bodega
      BigDecimal remaining = required.subtract(cocinaDeducted);
      if (remaining.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal bodegaDeducted = deductFromLocation(variantId, bodegaId, remaining);

        // If Bodega doesn't have enough either, throw exception
        if (bodegaDeducted.compareTo(remaining) < 0) {
          throw new InsufficientStockException(
              variantId, required, cocinaDeducted.add(bodegaDeducted));
        }
      }

      // Register movements
      if (cocinaDeducted.compareTo(BigDecimal.ZERO) > 0) {
        registerMovement(variantId, cocinaId, cocinaDeducted, orderId);
      }
      if (remaining.compareTo(BigDecimal.ZERO) > 0) {
        registerMovement(variantId, bodegaId, remaining, orderId);
      }
    }
  }

  @Override
  @Transactional
  public void revertDeductionsForOrder(Long orderId, List<OrderDetail> details) {
    // Consolidate all required supply variants and quantities
    Map<Long, BigDecimal> requiredVariants = new HashMap<>();

    for (OrderDetail detail : details) {
      // Add product recipe requirements
      List<ProductRecipe> productRecipes =
          productRecipeRepositoryPort.findByProductId(detail.getProduct().getId());
      for (ProductRecipe recipe : productRecipes) {
        requiredVariants.merge(
            recipe.getSupplyVariantId(), recipe.getRequiredQuantity(), BigDecimal::add);
      }

      // Add option recipe requirements
      if (detail.getSelectedOptions() != null && !detail.getSelectedOptions().isEmpty()) {
        List<Long> optionIds =
            detail.getSelectedOptions().stream().map(ProductOption::getId).toList();
        List<OptionRecipe> optionRecipes = optionRecipeRepositoryPort.findByOptionIdIn(optionIds);
        for (OptionRecipe recipe : optionRecipes) {
          requiredVariants.merge(
              recipe.getSupplyVariantId(), recipe.getRequiredQuantity(), BigDecimal::add);
        }
      }
    }

    // Get storage location IDs
    Long cocinaId = getStorageLocationId("Cocina");
    Long bodegaId = getStorageLocationId("Bodega");

    // Return stock to locations (reverse the deduction logic)
    for (Map.Entry<Long, BigDecimal> entry : requiredVariants.entrySet()) {
      Long variantId = entry.getKey();
      BigDecimal quantity = entry.getValue();

      // First, try to return to Bodega (where the excess was deducted from)
      BigDecimal bodegaReturned = returnToLocation(variantId, bodegaId, quantity);

      // If not all was returned to Bodega, return the rest to Cocina
      BigDecimal remaining = quantity.subtract(bodegaReturned);
      if (remaining.compareTo(BigDecimal.ZERO) > 0) {
        returnToLocation(variantId, cocinaId, remaining);
      }

      // Register ENTRY movements
      if (bodegaReturned.compareTo(BigDecimal.ZERO) > 0) {
        registerRevertMovement(variantId, bodegaId, bodegaReturned, orderId);
      }
      if (remaining.compareTo(BigDecimal.ZERO) > 0) {
        registerRevertMovement(variantId, cocinaId, remaining, orderId);
      }
    }
  }

  private Long getStorageLocationId(String name) {
    return storageLocationRepositoryPort
        .findByName(name)
        .orElseThrow(() -> new StorageLocationNotFoundException(name))
        .getId();
  }

  private BigDecimal deductFromLocation(Long variantId, Long locationId, BigDecimal quantity) {
    InventoryStock stock =
        inventoryStockRepositoryPort
            .findByVariantAndLocationWithLock(variantId, locationId)
            .orElse(null);

    if (stock == null || stock.getCurrentQuantity().compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal toDeduct = quantity.min(stock.getCurrentQuantity());
    stock.setCurrentQuantity(stock.getCurrentQuantity().subtract(toDeduct));
    inventoryStockRepositoryPort.save(stock);

    return toDeduct;
  }

  private BigDecimal returnToLocation(Long variantId, Long locationId, BigDecimal quantity) {
    InventoryStock stock =
        inventoryStockRepositoryPort
            .findByVariantAndLocationWithLock(variantId, locationId)
            .orElse(null);

    if (stock == null) {
      // Create new stock entry if it doesn't exist
      stock =
          InventoryStock.builder()
              .supplyVariantId(variantId)
              .storageLocationId(locationId)
              .currentQuantity(BigDecimal.ZERO)
              .build();
    }

    stock.setCurrentQuantity(stock.getCurrentQuantity().add(quantity));
    inventoryStockRepositoryPort.save(stock);

    return quantity;
  }

  private void registerMovement(
      Long variantId, Long locationId, BigDecimal quantity, Long orderId) {
    InventoryMovement movement =
        InventoryMovement.builder()
            .supplyVariantId(variantId)
            .toStorageLocationId(locationId)
            .quantity(quantity)
            .movementType(MovementType.DEDUCTION)
            .referenceOrderId(orderId)
            .createdAt(LocalDateTime.now())
            .build();

    inventoryMovementRepositoryPort.save(movement);
  }

  private void registerRevertMovement(
      Long variantId, Long locationId, BigDecimal quantity, Long orderId) {
    InventoryMovement movement =
        InventoryMovement.builder()
            .supplyVariantId(variantId)
            .fromStorageLocationId(locationId)
            .quantity(quantity)
            .movementType(MovementType.ENTRY)
            .referenceOrderId(orderId)
            .createdAt(LocalDateTime.now())
            .build();

    inventoryMovementRepositoryPort.save(movement);
  }
}
