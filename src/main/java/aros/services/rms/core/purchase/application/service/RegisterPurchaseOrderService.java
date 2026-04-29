/* (C) 2026 */

package aros.services.rms.core.purchase.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.inventory.application.exception.StorageLocationNotFoundException;
import aros.services.rms.core.inventory.application.exception.SupplyVariantNotFoundException;
import aros.services.rms.core.inventory.application.service.InventoryMovementService;
import aros.services.rms.core.inventory.domain.MovementType;
import aros.services.rms.core.inventory.port.output.InventoryMovementRepositoryPort;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.purchase.application.exception.InvalidPurchaseItemException;
import aros.services.rms.core.purchase.application.exception.SupplierInactiveException;
import aros.services.rms.core.purchase.application.exception.SupplierNotFoundException;
import aros.services.rms.core.purchase.domain.PurchaseOrder;
import aros.services.rms.core.purchase.domain.PurchaseOrderItem;
import aros.services.rms.core.purchase.port.input.RegisterPurchaseOrderUseCase;
import aros.services.rms.core.purchase.port.output.PurchaseOrderRepositoryPort;
import aros.services.rms.core.purchase.port.output.SupplierRepositoryPort;
import java.math.BigDecimal;

/**
 * Pure business logic for registering a purchase order.
 *
 * <p>This class is framework-agnostic: no Spring annotations, no @Transactional. Transaction
 * boundaries are managed by RegisterPurchaseOrderService in the infrastructure layer, which wraps
 * this use case and ensures all DB operations run in a single transaction.
 *
 * <p>Responsibilities:
 *
 * <ol>
 *   <li>Validate supplier exists and is active.
 *   <li>Validate each item's supply variant exists.
 *   <li>Validate quantityReceived <= quantityOrdered per item.
 *   <li>Persist the purchase order (cascade saves items).
 *   <li>For each item with quantityReceived > 0: upsert stock in Bodega + register ENTRY movement.
 * </ol>
 */
public class RegisterPurchaseOrderService implements RegisterPurchaseOrderUseCase {

  private final SupplierRepositoryPort supplierRepositoryPort;
  private final PurchaseOrderRepositoryPort purchaseOrderRepositoryPort;
  private final SupplyVariantRepositoryPort supplyVariantRepositoryPort;
  private final StorageLocationRepositoryPort storageLocationRepositoryPort;
  private final InventoryStockRepositoryPort inventoryStockRepositoryPort;
  private final InventoryMovementRepositoryPort inventoryMovementRepositoryPort;
  private final InventoryMovementService inventoryMovementHelper;
  private final Logger logger;

  /**
   * Creates the register purchase order service.
   *
   * @param supplierRepositoryPort repository for suppliers
   * @param purchaseOrderRepositoryPort repository for purchase orders
   * @param supplyVariantRepositoryPort repository for supply variants
   * @param storageLocationRepositoryPort repository for storage locations
   * @param inventoryStockRepositoryPort repository for inventory stock
   * @param inventoryMovementRepositoryPort repository for inventory movements
   * @param inventoryMovementHelper helper for inventory movements
   * @param logger logger instance
   */
  public RegisterPurchaseOrderService(
      SupplierRepositoryPort supplierRepositoryPort,
      PurchaseOrderRepositoryPort purchaseOrderRepositoryPort,
      SupplyVariantRepositoryPort supplyVariantRepositoryPort,
      StorageLocationRepositoryPort storageLocationRepositoryPort,
      InventoryStockRepositoryPort inventoryStockRepositoryPort,
      InventoryMovementRepositoryPort inventoryMovementRepositoryPort,
      InventoryMovementService inventoryMovementHelper,
      Logger logger) {
    this.supplierRepositoryPort = supplierRepositoryPort;
    this.purchaseOrderRepositoryPort = purchaseOrderRepositoryPort;
    this.supplyVariantRepositoryPort = supplyVariantRepositoryPort;
    this.storageLocationRepositoryPort = storageLocationRepositoryPort;
    this.inventoryStockRepositoryPort = inventoryStockRepositoryPort;
    this.inventoryMovementRepositoryPort = inventoryMovementRepositoryPort;
    this.inventoryMovementHelper = inventoryMovementHelper;
    this.logger = logger;
  }

  /**
   * Registers a new purchase order and integrates it with the inventory.
   *
   * @param order domain object with supplier, registeredBy, purchasedAt, totalAmount and items
   * @return the persisted purchase order with generated id
   * @throws SupplierNotFoundException if the supplier does not exist
   * @throws SupplierInactiveException if the supplier is inactive
   * @throws SupplyVariantNotFoundException if any item references a non-existent supply variant
   * @throws InvalidPurchaseItemException if quantityReceived > quantityOrdered for any item
   */
  @Override
  public PurchaseOrder register(PurchaseOrder order) {
    // 1. Validate supplier exists and is active
    var supplier =
        supplierRepositoryPort
            .findById(order.getSupplierId())
            .orElseThrow(() -> new SupplierNotFoundException(order.getSupplierId()));

    if (!supplier.isActive()) {
      throw new SupplierInactiveException(supplier.getId());
    }

    // 2. Validate each item
    for (PurchaseOrderItem item : order.getItems()) {
      // Validate supply variant exists
      if (!supplyVariantRepositoryPort.existsById(item.getSupplyVariantId())) {
        throw new SupplyVariantNotFoundException(item.getSupplyVariantId());
      }

      // Validate received quantity does not exceed ordered quantity
      if (item.getQuantityReceived().compareTo(item.getQuantityOrdered()) > 0) {
        throw new InvalidPurchaseItemException(item.getSupplyVariantId());
      }

      // Log damaged goods when received < ordered
      if (item.getQuantityReceived().compareTo(item.getQuantityOrdered()) < 0) {
        logger.info(
            "Damaged goods: variantId={}, ordered={}, received={}, purchaseOrderId=pending",
            item.getSupplyVariantId(),
            item.getQuantityOrdered(),
            item.getQuantityReceived());
      }
    }

    // 3. Persist the purchase order (cascade saves items via JPA)
    var savedOrder = purchaseOrderRepositoryPort.save(order);
    logger.info(
        "Purchase registered: id={}, supplier={}, total={}, registeredBy={}",
        savedOrder.getId(),
        savedOrder.getSupplierId(),
        savedOrder.getTotalAmount(),
        savedOrder.getRegisteredById());

    // 4. Resolve Bodega storage location id
    var bodegaId =
        storageLocationRepositoryPort
            .findByName("Bodega")
            .orElseThrow(() -> new StorageLocationNotFoundException("Bodega"))
            .getId();

    // 5. Integrate each item with inventory (only items with quantity received > 0)
    for (PurchaseOrderItem item : savedOrder.getItems()) {
      if (item.getQuantityReceived().compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }

      // Upsert stock in Bodega using the shared helper from InventoryMovementUseCaseImpl
      inventoryMovementHelper.addStockToLocation(
          item.getSupplyVariantId(), bodegaId, item.getQuantityReceived());

      // Register ENTRY movement referencing this purchase order
      inventoryMovementHelper.registerMovement(
          item.getSupplyVariantId(),
          null, // fromLocation = null (external entry)
          bodegaId,
          item.getQuantityReceived(),
          MovementType.ENTRY,
          null, // no sales order reference
          savedOrder.getId());

      logger.info(
          "Stock entry: variantId={}, location=Bodega, qty={}, purchaseOrderId={}",
          item.getSupplyVariantId(),
          item.getQuantityReceived(),
          savedOrder.getId());
    }

    return savedOrder;
  }
}
