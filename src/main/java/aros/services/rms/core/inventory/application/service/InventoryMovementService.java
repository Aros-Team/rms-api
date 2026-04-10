/* (C) 2026 */
package aros.services.rms.core.inventory.application.service;

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

/**
 * Pure business logic for inventory movement operations.
 *
 * <p>This class is framework-agnostic: no Spring annotations, no @Transactional. Transaction
 * boundaries are managed by the infrastructure service that wraps this use case
 * (InventoryMovementService in the infrastructure layer).
 */
public class InventoryMovementService implements InventoryMovementUseCase {

  private final ProductRecipeRepositoryPort productRecipeRepositoryPort;
  private final OptionRecipeRepositoryPort optionRecipeRepositoryPort;
  private final InventoryStockRepositoryPort inventoryStockRepositoryPort;
  private final InventoryMovementRepositoryPort inventoryMovementRepositoryPort;
  private final StorageLocationRepositoryPort storageLocationRepositoryPort;

  public InventoryMovementService(
      ProductRecipeRepositoryPort productRecipeRepositoryPort,
      OptionRecipeRepositoryPort optionRecipeRepositoryPort,
      InventoryStockRepositoryPort inventoryStockRepositoryPort,
      InventoryMovementRepositoryPort inventoryMovementRepositoryPort,
      StorageLocationRepositoryPort storageLocationRepositoryPort) {
    this.productRecipeRepositoryPort = productRecipeRepositoryPort;
    this.optionRecipeRepositoryPort = optionRecipeRepositoryPort;
    this.inventoryStockRepositoryPort = inventoryStockRepositoryPort;
    this.inventoryMovementRepositoryPort = inventoryMovementRepositoryPort;
    this.storageLocationRepositoryPort = storageLocationRepositoryPort;
  }

  /**
   * Deducts inventory stock for a completed order. Deducts from Cocina first, then Bodega as
   * fallback. Registers a DEDUCTION movement per variant per location used.
   *
   * @param orderId the order id used as reference in movements
   * @param details the order details containing products and selected options
   * @throws InsufficientStockException if combined stock is not enough
   */
  @Override
  public void deductForOrder(Long orderId, List<OrderDetail> details) {
    // Consolidate all required supply variants and quantities across all order details
    Map<Long, BigDecimal> requiredVariants = buildRequiredVariantsMap(details);

    Long cocinaId = getStorageLocationId("Cocina");
    Long bodegaId = getStorageLocationId("Bodega");

    for (Map.Entry<Long, BigDecimal> entry : requiredVariants.entrySet()) {
      Long variantId = entry.getKey();
      BigDecimal required = entry.getValue();

      // Try Cocina first
      BigDecimal cocinaDeducted = deductFromLocation(variantId, cocinaId, required);

      // Deduct remainder from Bodega if Cocina was not enough
      BigDecimal remaining = required.subtract(cocinaDeducted);
      if (remaining.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal bodegaDeducted = deductFromLocation(variantId, bodegaId, remaining);
        if (bodegaDeducted.compareTo(remaining) < 0) {
          throw new InsufficientStockException(
              variantId, required, cocinaDeducted.add(bodegaDeducted));
        }
      }

      // Register movements only for quantities actually deducted
      if (cocinaDeducted.compareTo(BigDecimal.ZERO) > 0) {
        registerMovement(
            variantId, cocinaId, null, cocinaDeducted, MovementType.DEDUCTION, orderId, null);
      }
      if (remaining.compareTo(BigDecimal.ZERO) > 0) {
        registerMovement(
            variantId, bodegaId, null, remaining, MovementType.DEDUCTION, orderId, null);
      }
    }
  }

  /**
   * Reverts inventory deductions for a cancelled order. Returns stock to original locations in
   * reverse order (Bodega first, then Cocina).
   *
   * @param orderId the order id used as reference in revert movements
   * @param details the order details to revert
   */
  @Override
  public void revertDeductionsForOrder(Long orderId, List<OrderDetail> details) {
    Map<Long, BigDecimal> requiredVariants = buildRequiredVariantsMap(details);

    Long cocinaId = getStorageLocationId("Cocina");
    Long bodegaId = getStorageLocationId("Bodega");

    for (Map.Entry<Long, BigDecimal> entry : requiredVariants.entrySet()) {
      Long variantId = entry.getKey();
      BigDecimal quantity = entry.getValue();

      // Return to Bodega first (reverse of deduction order)
      BigDecimal bodegaReturned = returnToLocation(variantId, bodegaId, quantity);
      BigDecimal remaining = quantity.subtract(bodegaReturned);
      if (remaining.compareTo(BigDecimal.ZERO) > 0) {
        returnToLocation(variantId, cocinaId, remaining);
      }

      // Register ENTRY movements for the revert
      if (bodegaReturned.compareTo(BigDecimal.ZERO) > 0) {
        registerMovement(
            variantId, null, bodegaId, bodegaReturned, MovementType.ENTRY, orderId, null);
      }
      if (remaining.compareTo(BigDecimal.ZERO) > 0) {
        registerMovement(variantId, null, cocinaId, remaining, MovementType.ENTRY, orderId, null);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Package-visible helpers reused by RegisterPurchaseOrderUseCaseImpl
  // ---------------------------------------------------------------------------

  /**
   * Adds stock to a location (upsert). Creates the stock record if it does not exist yet.
   *
   * @param variantId supply variant identifier
   * @param locationId storage location identifier
   * @param quantity amount to add
   */
  public void addStockToLocation(Long variantId, Long locationId, BigDecimal quantity) {
    InventoryStock stock =
        inventoryStockRepositoryPort
            .findByVariantAndLocationWithLock(variantId, locationId)
            .orElse(
                InventoryStock.builder()
                    .supplyVariantId(variantId)
                    .storageLocationId(locationId)
                    .currentQuantity(BigDecimal.ZERO)
                    .build());

    stock.setCurrentQuantity(stock.getCurrentQuantity().add(quantity));
    inventoryStockRepositoryPort.save(stock);
  }

  /**
   * Registers a generic inventory movement.
   *
   * @param variantId supply variant
   * @param fromLocationId origin location (null if external entry)
   * @param toLocationId destination location (null if deduction)
   * @param quantity movement quantity
   * @param type ENTRY or DEDUCTION
   * @param referenceOrderId sales order reference (nullable)
   * @param referencePurchaseOrderId purchase order reference (nullable)
   */
  public void registerMovement(
      Long variantId,
      Long fromLocationId,
      Long toLocationId,
      BigDecimal quantity,
      MovementType type,
      Long referenceOrderId,
      Long referencePurchaseOrderId) {
    var movement =
        InventoryMovement.builder()
            .supplyVariantId(variantId)
            .fromStorageLocationId(fromLocationId)
            .toStorageLocationId(toLocationId)
            .quantity(quantity)
            .movementType(type)
            .referenceOrderId(referenceOrderId)
            .referencePurchaseOrderId(referencePurchaseOrderId)
            .createdAt(LocalDateTime.now())
            .build();
    inventoryMovementRepositoryPort.save(movement);
  }

  // ---------------------------------------------------------------------------
  // Private helpers
  // ---------------------------------------------------------------------------

  /** Builds a consolidated map of supplyVariantId → total required quantity from order details. */
  private Map<Long, BigDecimal> buildRequiredVariantsMap(List<OrderDetail> details) {
    Map<Long, BigDecimal> required = new HashMap<>();
    for (OrderDetail detail : details) {
      List<ProductRecipe> productRecipes =
          productRecipeRepositoryPort.findByProductId(detail.getProduct().getId());
      for (ProductRecipe recipe : productRecipes) {
        required.merge(recipe.getSupplyVariantId(), recipe.getRequiredQuantity(), BigDecimal::add);
      }
      if (detail.getSelectedOptions() != null && !detail.getSelectedOptions().isEmpty()) {
        List<Long> optionIds =
            detail.getSelectedOptions().stream().map(ProductOption::getId).toList();
        List<OptionRecipe> optionRecipes = optionRecipeRepositoryPort.findByOptionIdIn(optionIds);
        for (OptionRecipe recipe : optionRecipes) {
          required.merge(
              recipe.getSupplyVariantId(), recipe.getRequiredQuantity(), BigDecimal::add);
        }
      }
    }
    return required;
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
            .orElse(
                InventoryStock.builder()
                    .supplyVariantId(variantId)
                    .storageLocationId(locationId)
                    .currentQuantity(BigDecimal.ZERO)
                    .build());
    stock.setCurrentQuantity(stock.getCurrentQuantity().add(quantity));
    inventoryStockRepositoryPort.save(stock);
    return quantity;
  }
}
