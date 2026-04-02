/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.persistence.jpa;

import aros.services.rms.core.purchase.domain.PurchaseOrder;
import aros.services.rms.infraestructure.purchase.persistence.PurchaseOrderEntity;
import aros.services.rms.infraestructure.purchase.persistence.SupplierEntity;
import aros.services.rms.infraestructure.user.persistence.jpa.UserEntity;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Mapper between PurchaseOrder domain model and PurchaseOrderEntity JPA entity. */
@Component
public class PurchaseOrderMapper {

  private final PurchaseOrderItemMapper itemMapper;

  public PurchaseOrderMapper(PurchaseOrderItemMapper itemMapper) {
    this.itemMapper = itemMapper;
  }

  public PurchaseOrder toDomain(PurchaseOrderEntity entity) {
    if (entity == null) return null;
    return PurchaseOrder.builder()
        .id(entity.getId())
        .supplierId(entity.getSupplier() != null ? entity.getSupplier().getId() : null)
        .registeredById(entity.getRegisteredBy() != null ? entity.getRegisteredBy().getId() : null)
        .purchasedAt(entity.getPurchasedAt())
        .totalAmount(entity.getTotalAmount())
        .notes(entity.getNotes())
        .createdAt(entity.getCreatedAt())
        .items(
            entity.getItems() != null
                ? entity.getItems().stream().map(itemMapper::toDomain).collect(Collectors.toList())
                : Collections.emptyList())
        .build();
  }

  public PurchaseOrderEntity toEntity(PurchaseOrder domain) {
    if (domain == null) return null;
    var entity =
        PurchaseOrderEntity.builder()
            .id(domain.getId())
            .purchasedAt(domain.getPurchasedAt())
            .totalAmount(domain.getTotalAmount())
            .notes(domain.getNotes())
            // createdAt is set by the server on insert; never trust the domain value
            .createdAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : LocalDateTime.now())
            .build();

    if (domain.getSupplierId() != null) {
      entity.setSupplier(SupplierEntity.builder().id(domain.getSupplierId()).build());
    }

    if (domain.getRegisteredById() != null) {
      var user = new UserEntity();
      user.setId(domain.getRegisteredById());
      entity.setRegisteredBy(user);
    }

    if (domain.getItems() != null) {
      var items =
          domain.getItems().stream().map(itemMapper::toEntity).collect(Collectors.toList());
      // set back-reference so JPA cascade works correctly
      items.forEach(item -> item.setPurchaseOrder(entity));
      entity.setItems(items);
    }

    return entity;
  }
}
