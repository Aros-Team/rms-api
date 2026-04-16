/* (C) 2026 */
package aros.services.rms.core.inventory.application.service;

import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.application.exception.StorageLocationNotFoundException;
import aros.services.rms.core.inventory.application.exception.SupplyVariantNotFoundException;
import aros.services.rms.core.inventory.domain.InventoryMovement;
import aros.services.rms.core.inventory.domain.InventoryStock;
import aros.services.rms.core.inventory.domain.MovementType;
import aros.services.rms.core.inventory.port.input.TransferInventoryUseCase;
import aros.services.rms.core.inventory.port.output.InventoryMovementRepositoryPort;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.core.inventory.port.output.StorageLocationRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure business logic for inventory transfer operations.
 *
 * <p>This class is framework-agnostic: no Spring annotations, no @Transactional. Transaction
 * boundaries are managed by the infrastructure service that wraps this use case.
 */
public class TransferInventoryService implements TransferInventoryUseCase {

  private final SupplyVariantRepositoryPort supplyVariantRepositoryPort;
  private final InventoryStockRepositoryPort inventoryStockRepositoryPort;
  private final InventoryMovementRepositoryPort inventoryMovementRepositoryPort;
  private final StorageLocationRepositoryPort storageLocationRepositoryPort;

  public TransferInventoryService(
      SupplyVariantRepositoryPort supplyVariantRepositoryPort,
      InventoryStockRepositoryPort inventoryStockRepositoryPort,
      InventoryMovementRepositoryPort inventoryMovementRepositoryPort,
      StorageLocationRepositoryPort storageLocationRepositoryPort) {
    this.supplyVariantRepositoryPort = supplyVariantRepositoryPort;
    this.inventoryStockRepositoryPort = inventoryStockRepositoryPort;
    this.inventoryMovementRepositoryPort = inventoryMovementRepositoryPort;
    this.storageLocationRepositoryPort = storageLocationRepositoryPort;
  }

  /**
   * Transfers multiple supply variants from Bodega to Cocina. Operation is atomic: all transfers
   * succeed or all fail.
   *
   * @param items list of transfer items
   * @return list of registered movements
   * @throws SupplyVariantNotFoundException if any variant does not exist
   * @throws InsufficientStockException if any variant has insufficient stock in Bodega
   * @throws StorageLocationNotFoundException if Bodega or Cocina locations are not found
   */
  @Override
  public List<InventoryMovement> transferToKitchen(List<TransferItem> items) {
    Long bodegaId = getStorageLocationId("Bodega");
    Long cocinaId = getStorageLocationId("Cocina");

    List<InventoryMovement> movements = new ArrayList<>();

    for (TransferItem item : items) {
      // Validate variant exists
      if (!supplyVariantRepositoryPort.existsById(item.supplyVariantId())) {
        throw new SupplyVariantNotFoundException(item.supplyVariantId());
      }

      // Validate quantity is positive
      if (item.quantity().compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException(
            "Quantity must be greater than zero for variant: " + item.supplyVariantId());
      }

      // Get stock from Bodega with pessimistic lock
      InventoryStock bodegaStock =
          inventoryStockRepositoryPort
              .findByVariantAndLocationWithLock(item.supplyVariantId(), bodegaId)
              .orElseThrow(
                  () ->
                      new InsufficientStockException(
                          item.supplyVariantId(),
                          MovementType.TRANSFER,
                          String.format(
                              "No stock record found in Bodega for variant %d",
                              item.supplyVariantId())));

      // Validate sufficient stock
      if (bodegaStock.getCurrentQuantity().compareTo(item.quantity()) < 0) {
        throw new InsufficientStockException(
            item.supplyVariantId(), item.quantity(), bodegaStock.getCurrentQuantity());
      }

      // Deduct from Bodega
      bodegaStock.setCurrentQuantity(bodegaStock.getCurrentQuantity().subtract(item.quantity()));
      inventoryStockRepositoryPort.save(bodegaStock);

      // Add to Cocina (create if not exists)
      InventoryStock cocinaStock =
          inventoryStockRepositoryPort
              .findByVariantAndLocationWithLock(item.supplyVariantId(), cocinaId)
              .orElse(
                  InventoryStock.builder()
                      .supplyVariantId(item.supplyVariantId())
                      .storageLocationId(cocinaId)
                      .currentQuantity(BigDecimal.ZERO)
                      .build());

      cocinaStock.setCurrentQuantity(cocinaStock.getCurrentQuantity().add(item.quantity()));
      inventoryStockRepositoryPort.save(cocinaStock);

      // Register movement
      InventoryMovement movement =
          InventoryMovement.builder()
              .supplyVariantId(item.supplyVariantId())
              .fromStorageLocationId(bodegaId)
              .toStorageLocationId(cocinaId)
              .quantity(item.quantity())
              .movementType(MovementType.TRANSFER)
              .createdAt(LocalDateTime.now())
              .build();

      InventoryMovement savedMovement = inventoryMovementRepositoryPort.save(movement);
      movements.add(savedMovement);
    }

    return movements;
  }

  private Long getStorageLocationId(String name) {
    return storageLocationRepositoryPort
        .findByName(name)
        .orElseThrow(() -> new StorageLocationNotFoundException(name))
        .getId();
  }
}
