/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa.adapters;

import aros.services.rms.core.inventory.domain.InventoryStock;
import aros.services.rms.core.inventory.port.output.InventoryStockRepositoryPort;
import aros.services.rms.infraestructure.inventory.persistence.InventoryStockEntity;
import aros.services.rms.infraestructure.inventory.persistence.StorageLocationEntity;
import aros.services.rms.infraestructure.inventory.persistence.SupplyVariantEntity;
import aros.services.rms.infraestructure.inventory.persistence.jpa.InventoryStockMapper;
import aros.services.rms.infraestructure.inventory.persistence.jpa.InventoryStockRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.StorageLocationRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyVariantRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adapter that connects InventoryStockRepositoryPort with JPA repository. */
@Component
@RequiredArgsConstructor
public class InventoryStockPersistenceAdapter implements InventoryStockRepositoryPort {

  private final InventoryStockRepository inventoryStockRepository;
  private final InventoryStockMapper inventoryStockMapper;
  private final SupplyVariantRepository supplyVariantRepository;
  private final StorageLocationRepository storageLocationRepository;

  @Override
  public Optional<InventoryStock> findByVariantAndLocationWithLock(
      Long variantId, Long locationId) {
    return inventoryStockRepository
        .findByVariantAndLocationWithLock(variantId, locationId)
        .map(inventoryStockMapper::toDomain);
  }

  @Override
  @Transactional
  public InventoryStock save(InventoryStock inventoryStock) {
    InventoryStockEntity entity;

    if (inventoryStock.getId() != null) {
      // Update: load existing entity to preserve JPA relationships, only update quantity
      entity =
          inventoryStockRepository
              .findById(inventoryStock.getId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "InventoryStock not found: " + inventoryStock.getId()));
      entity.setCurrentQuantity(inventoryStock.getCurrentQuantity());
    } else {
      // Insert: resolve JPA relationships from IDs
      SupplyVariantEntity supplyVariant =
          supplyVariantRepository
              .findById(inventoryStock.getSupplyVariantId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "SupplyVariant not found: " + inventoryStock.getSupplyVariantId()));
      StorageLocationEntity storageLocation =
          storageLocationRepository
              .findById(inventoryStock.getStorageLocationId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "StorageLocation not found: " + inventoryStock.getStorageLocationId()));
      entity =
          InventoryStockEntity.builder()
              .supplyVariant(supplyVariant)
              .storageLocation(storageLocation)
              .currentQuantity(inventoryStock.getCurrentQuantity())
              .build();
    }

    return inventoryStockMapper.toDomain(inventoryStockRepository.save(entity));
  }
}
