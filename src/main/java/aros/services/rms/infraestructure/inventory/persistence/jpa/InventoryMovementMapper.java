/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.core.inventory.domain.InventoryMovement;
import aros.services.rms.infraestructure.inventory.persistence.InventoryMovementEntity;
import aros.services.rms.infraestructure.inventory.persistence.StorageLocationEntity;
import aros.services.rms.infraestructure.inventory.persistence.SupplyVariantEntity;
import aros.services.rms.infraestructure.order.persistence.Order;
import org.springframework.stereotype.Component;

/** Mapper between InventoryMovement domain model and InventoryMovementEntity JPA entity. */
@Component
public class InventoryMovementMapper {

  /** Converts an InventoryMovementEntity JPA entity to a domain model. */
  public InventoryMovement toDomain(InventoryMovementEntity entity) {
    if (entity == null) return null;
    return InventoryMovement.builder()
        .id(entity.getId())
        .supplyVariantId(
            entity.getSupplyVariant() != null ? entity.getSupplyVariant().getId() : null)
        .fromStorageLocationId(
            entity.getFromStorageLocation() != null
                ? entity.getFromStorageLocation().getId()
                : null)
        .toStorageLocationId(
            entity.getToStorageLocation() != null ? entity.getToStorageLocation().getId() : null)
        .quantity(entity.getQuantity())
        .movementType(entity.getMovementType())
        .referenceOrderId(
            entity.getReferenceOrder() != null ? entity.getReferenceOrder().getId() : null)
        .createdAt(entity.getCreatedAt())
        .build();
  }

  /** Converts an InventoryMovement domain model to a JPA entity. */
  public InventoryMovementEntity toEntity(InventoryMovement domain) {
    if (domain == null) return null;
    InventoryMovementEntity entity =
        InventoryMovementEntity.builder()
            .id(domain.getId())
            .quantity(domain.getQuantity())
            .movementType(domain.getMovementType())
            .createdAt(domain.getCreatedAt())
            .build();

    if (domain.getSupplyVariantId() != null) {
      entity.setSupplyVariant(
          SupplyVariantEntity.builder().id(domain.getSupplyVariantId()).build());
    }

    if (domain.getFromStorageLocationId() != null) {
      entity.setFromStorageLocation(
          StorageLocationEntity.builder().id(domain.getFromStorageLocationId()).build());
    }

    if (domain.getToStorageLocationId() != null) {
      entity.setToStorageLocation(
          StorageLocationEntity.builder().id(domain.getToStorageLocationId()).build());
    }

    if (domain.getReferenceOrderId() != null) {
      entity.setReferenceOrder(Order.builder().id(domain.getReferenceOrderId()).build());
    }

    return entity;
  }
}
