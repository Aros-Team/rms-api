/* (C) 2026 */

package aros.services.rms.core.inventory.application.service;

import aros.services.rms.core.category.domain.OptionGroup;
import aros.services.rms.core.category.domain.OptionSelectionType;
import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.common.notification.port.output.NotificationPort;
import aros.services.rms.core.inventory.application.dto.InventoryStockUpdatedEvent;
import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.application.exception.StorageLocationNotFoundException;
import aros.services.rms.core.inventory.domain.InventoryMovement;
import aros.services.rms.core.inventory.domain.InventoryStock;
import aros.services.rms.core.inventory.domain.MovementType;
import aros.services.rms.core.inventory.domain.OptionRecipe;
import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.core.inventory.domain.StorageLocation;
import aros.services.rms.core.inventory.port.input.InventoryMovementUseCase;
import aros.services.rms.core.inventory.port.output.InventoryMovementRepositoryPort;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.OptionRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Pure business logic for inventory movement operations.
 *
 * <p>This class is framework-agnostic: no Spring annotations, no @Transactional. Transaction
 * boundaries are managed by the infrastructure service that wraps this use case
 * (InventoryMovementService in the infrastructure layer).
 */
public class InventoryMovementService implements InventoryMovementUseCase {

  /** Topic STOMP al que se publican los cambios de stock en tiempo real. */
  public static final String INVENTORY_UPDATES_TOPIC = "/topic/inventory/updates";

  private final ProductRecipeRepositoryPort productRecipeRepositoryPort;
  private final OptionRecipeRepositoryPort optionRecipeRepositoryPort;
  private final InventoryStockRepositoryPort inventoryStockRepositoryPort;
  private final InventoryMovementRepositoryPort inventoryMovementRepositoryPort;
  private final StorageLocationRepositoryPort storageLocationRepositoryPort;
  private final BusinessMetricsPort metricsPort;
  private final NotificationPort notificationPort;
  private final ProductOptionRepositoryPort productOptionRepositoryPort;

  /**
   * Creates a new inventory movement service instance.
   *
   * @param productRecipeRepositoryPort the product recipe repository port
   * @param optionRecipeRepositoryPort the option recipe repository port
   * @param inventoryStockRepositoryPort the inventory stock repository port
   * @param inventoryMovementRepositoryPort the inventory movement repository port
   * @param storageLocationRepositoryPort the storage location repository port
   * @param metricsPort the business metrics port
   * @param notificationPort the notification port for real-time WebSocket events
   * @param productOptionRepositoryPort the product option repository port (Phase D: supplies slot
   *     info for substitution semantics)
   */
  public InventoryMovementService(
      ProductRecipeRepositoryPort productRecipeRepositoryPort,
      OptionRecipeRepositoryPort optionRecipeRepositoryPort,
      InventoryStockRepositoryPort inventoryStockRepositoryPort,
      InventoryMovementRepositoryPort inventoryMovementRepositoryPort,
      StorageLocationRepositoryPort storageLocationRepositoryPort,
      BusinessMetricsPort metricsPort,
      NotificationPort notificationPort,
      ProductOptionRepositoryPort productOptionRepositoryPort) {
    this.productRecipeRepositoryPort = productRecipeRepositoryPort;
    this.optionRecipeRepositoryPort = optionRecipeRepositoryPort;
    this.inventoryStockRepositoryPort = inventoryStockRepositoryPort;
    this.inventoryMovementRepositoryPort = inventoryMovementRepositoryPort;
    this.storageLocationRepositoryPort = storageLocationRepositoryPort;
    this.metricsPort = metricsPort;
    this.notificationPort = notificationPort;
    this.productOptionRepositoryPort = productOptionRepositoryPort;
  }

  /**
   * Deducts inventory stock for a completed order. Deducts from Cocina first, then Bodega as
   * fallback. Registers a DEDUCTION movement per variant per location used. After all deductions
   * are applied, publishes a {@link InventoryStockUpdatedEvent} to {@value INVENTORY_UPDATES_TOPIC}
   * so connected clients can refresh their inventory view in real time.
   *
   * @param orderId the order id used as reference in movements
   * @param details the order details containing products and selected options
   * @throws InsufficientStockException if combined stock is not enough
   */
  @Override
  public void deductForOrder(Long orderId, List<OrderDetail> details) {
    // Consolidate all required supply variants and quantities across all order details
    Map<Long, BigDecimal> requiredVariants = buildRequiredVariantsMap(details);

    StorageLocation cocina = getStorageLocation("Cocina");
    StorageLocation bodega = getStorageLocation("Bodega");
    Long cocinaId = cocina.getId();
    Long bodegaId = bodega.getId();

    List<InventoryStockUpdatedEvent.UpdatedStockItem> updatedItems = new ArrayList<>();

    for (Map.Entry<Long, BigDecimal> entry : requiredVariants.entrySet()) {
      Long variantId = entry.getKey();
      BigDecimal required = entry.getValue();
      // Phase D — semantics: substitution and REMOVE options can drive the net required to zero
      // (base recipe line cancelled by the option). Skip the deduction loop entirely in that case.
      if (required == null || required.signum() <= 0) {
        continue;
      }

      // Try Cocina first
      BigDecimal cocinaDeducted = deductFromLocation(variantId, cocinaId, required);

      // Deduct remainder from Bodega if Cocina was not enough
      BigDecimal remaining = required.subtract(cocinaDeducted);
      if (remaining.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal bodegaDeducted = deductFromLocation(variantId, bodegaId, remaining);
        if (bodegaDeducted.compareTo(remaining) < 0) {
          metricsPort.recordFallbackFailed();
          throw new InsufficientStockException(
              variantId, required, cocinaDeducted.add(bodegaDeducted));
        }
        metricsPort.recordFallbackExecuted();
      }

      // Register movements only for quantities actually deducted
      if (cocinaDeducted.compareTo(BigDecimal.ZERO) > 0) {
        registerMovement(
            variantId, cocinaId, null, cocinaDeducted, MovementType.DEDUCTION, orderId, null);
        inventoryStockRepositoryPort
            .findByVariantAndLocationWithLock(variantId, cocinaId)
            .ifPresent(
                stock ->
                    updatedItems.add(
                        InventoryStockUpdatedEvent.UpdatedStockItem.builder()
                            .supplyVariantId(variantId)
                            .storageLocationId(cocinaId)
                            .locationName(cocina.getName())
                            .currentQuantity(stock.getCurrentQuantity())
                            .build()));
      }
      if (remaining.compareTo(BigDecimal.ZERO) > 0) {
        registerMovement(
            variantId, bodegaId, null, remaining, MovementType.DEDUCTION, orderId, null);
        inventoryStockRepositoryPort
            .findByVariantAndLocationWithLock(variantId, bodegaId)
            .ifPresent(
                stock ->
                    updatedItems.add(
                        InventoryStockUpdatedEvent.UpdatedStockItem.builder()
                            .supplyVariantId(variantId)
                            .storageLocationId(bodegaId)
                            .locationName(bodega.getName())
                            .currentQuantity(stock.getCurrentQuantity())
                            .build()));
      }
    }

    if (!updatedItems.isEmpty()) {
      notificationPort.notify(
          INVENTORY_UPDATES_TOPIC,
          InventoryStockUpdatedEvent.builder().updatedItems(updatedItems).build());
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

    Long cocinaId = getStorageLocation("Cocina").getId();
    Long bodegaId = getStorageLocation("Bodega").getId();

    for (Map.Entry<Long, BigDecimal> entry : requiredVariants.entrySet()) {
      Long variantId = entry.getKey();
      BigDecimal quantity = entry.getValue();
      // Phase D — semantics: skip non-positive required values (substitution / REMOVE cancel).
      if (quantity == null || quantity.signum() <= 0) {
        continue;
      }

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

  /**
   * Builds a consolidated map of supplyVariantId → total required quantity from order details.
   *
   * <p>Phase D — semantics: per order detail, selection-mode semantics are applied to the
   * customer's selected options. SINGLE_CHOICE options that declare a {@code
   * replace_supply_category_id} cause the base-recipe lines of that slot to be subtracted and the
   * selected option's recipe to be added. REMOVE selections subtract the option's recipe. Other
   * categories add the option's recipe (current behavior). Categories with no selection leave the
   * base recipe intact.
   */
  private Map<Long, BigDecimal> buildRequiredVariantsMap(List<OrderDetail> details) {
    Map<Long, BigDecimal> required = new HashMap<>();
    for (OrderDetail detail : details) {
      Long productId = detail.getProduct() == null ? null : detail.getProduct().getId();
      List<ProductRecipe> productRecipes =
          productId == null ? List.of() : productRecipeRepositoryPort.findByProductId(productId);
      for (ProductRecipe recipe : productRecipes) {
        required.merge(recipe.getSupplyVariantId(), recipe.getRequiredQuantity(), BigDecimal::add);
      }
      // Apply selection-mode semantics to the selected options (may subtract base-recipe lines for
      // substitution slots and add/subtract option recipes).
      if (detail.getSelectedOptions() != null && !detail.getSelectedOptions().isEmpty()) {
        applySelectionModeSemantics(productId, detail.getSelectedOptions(), required);
      }
      if (detail.getSelectedProductIds() != null && !detail.getSelectedProductIds().isEmpty()) {
        for (Long selectedProductId : detail.getSelectedProductIds()) {
          List<ProductRecipe> selectedRecipes =
              productRecipeRepositoryPort.findByProductId(selectedProductId);
          for (ProductRecipe recipe : selectedRecipes) {
            required.merge(
                recipe.getSupplyVariantId(), recipe.getRequiredQuantity(), BigDecimal::add);
          }
        }
      }
    }
    return required;
  }

  /**
   * Applies the selection-mode semantics to the required-variants map. See {@link
   * InventoryStockService} for the full rule set. The map is mutated in place.
   */
  private void applySelectionModeSemantics(
      Long productId, List<ProductOption> selectedOptions, Map<Long, BigDecimal> required) {
    if (selectedOptions == null || selectedOptions.isEmpty()) {
      return;
    }
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
            productId == null
                ? List.of()
                : productOptionRepositoryPort.loadBaseRecipeBySupplyCategory(
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
        subtractOptionRecipes(opts, required);
      } else {
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

  private StorageLocation getStorageLocation(String name) {
    return storageLocationRepositoryPort
        .findByName(name)
        .orElseThrow(() -> new StorageLocationNotFoundException(name));
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
