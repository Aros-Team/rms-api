/* (C) 2026 */

package aros.services.rms.infraestructure.purchase.persistence.jpa;

import aros.services.rms.core.purchase.domain.PurchaseOrderItem;
import aros.services.rms.infraestructure.inventory.persistence.SupplyVariantEntity;
import aros.services.rms.infraestructure.purchase.persistence.PurchaseOrderEntity;
import aros.services.rms.infraestructure.purchase.persistence.PurchaseOrderItemEntity;
import org.springframework.stereotype.Component;

/** Mapper between PurchaseOrderItem domain model and PurchaseOrderItemEntity JPA entity. */
@Component
public class PurchaseOrderItemMapper {

  /**
   * Converts an entity to domain.
   *
   * @param entity the entity
   * @return the domain
   */
  public PurchaseOrderItem toDomain(PurchaseOrderItemEntity entity) {
    if (entity == null) {
      return null;
    }
    return PurchaseOrderItem.builder()
        .id(entity.getId())
        .purchaseOrderId(
            entity.getPurchaseOrder() != null ? entity.getPurchaseOrder().getId() : null)
        .supplyVariantId(
            entity.getSupplyVariant() != null ? entity.getSupplyVariant().getId() : null)
        .quantityOrdered(entity.getQuantityOrdered())
        .quantityReceived(entity.getQuantityReceived())
        .unitPrice(entity.getUnitPrice())
        .build();
  }

  /**
   * Converts a domain to entity.
   *
   * @param domain the domain
   * @return the entity
   */
  public PurchaseOrderItemEntity toEntity(PurchaseOrderItem domain) {
    if (domain == null) {
      return null;
    }
    var entity =
        PurchaseOrderItemEntity.builder()
            .id(domain.getId())
            .quantityOrdered(domain.getQuantityOrdered())
            .quantityReceived(domain.getQuantityReceived())
            .unitPrice(domain.getUnitPrice())
            .build();

    if (domain.getPurchaseOrderId() != null) {
      entity.setPurchaseOrder(
          PurchaseOrderEntity.builder().id(domain.getPurchaseOrderId()).build());
    }

    if (domain.getSupplyVariantId() != null) {
      entity.setSupplyVariant(
          SupplyVariantEntity.builder().id(domain.getSupplyVariantId()).build());
    }

    return entity;
  }
}
