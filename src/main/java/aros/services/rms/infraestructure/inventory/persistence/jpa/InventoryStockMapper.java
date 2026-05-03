/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.core.inventory.domain.InventoryStock;
import aros.services.rms.infraestructure.inventory.persistence.InventoryStockEntity;
import org.springframework.stereotype.Component;

/** Mapper between InventoryStock domain model and InventoryStockEntity JPA entity. */
@Component
public class InventoryStockMapper {

  /** Converts an InventoryStockEntity JPA entity to a domain model. */
  public InventoryStock toDomain(InventoryStockEntity entity) {
    if (entity == null) {
      return null;
    }
    return InventoryStock.builder()
        .id(entity.getId())
        .supplyVariantId(
            entity.getSupplyVariant() != null ? entity.getSupplyVariant().getId() : null)
        .storageLocationId(
            entity.getStorageLocation() != null ? entity.getStorageLocation().getId() : null)
        .currentQuantity(entity.getCurrentQuantity())
        .build();
  }

  /** Converts an InventoryStock domain model to a JPA entity. */
  public InventoryStockEntity toEntity(InventoryStock domain) {
    if (domain == null) {
      return null;
    }
    return InventoryStockEntity.builder()
        .id(domain.getId())
        .currentQuantity(domain.getCurrentQuantity())
        .build();
  }
}
