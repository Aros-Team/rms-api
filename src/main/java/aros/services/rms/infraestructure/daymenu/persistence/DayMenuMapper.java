/* (C) 2026 */
package aros.services.rms.infraestructure.daymenu.persistence;

import aros.services.rms.core.daymenu.domain.DayMenu;
import aros.services.rms.core.daymenu.domain.DayMenuHistory;
import aros.services.rms.infraestructure.product.persistence.Product;
import org.springframework.stereotype.Component;

/** Maps between DayMenu/DayMenuHistory domain models and JPA entities. */
@Component
public class DayMenuMapper {

  public DayMenu toDomain(DayMenuEntity entity) {
    return DayMenu.builder()
        .id(entity.getId())
        .productId(entity.getProduct().getId())
        .productName(entity.getProduct().getName())
        .productBasePrice(entity.getProduct().getBasePrice())
        .validFrom(entity.getValidFrom())
        .createdBy(entity.getCreatedBy())
        .build();
  }

  public DayMenuEntity toEntity(DayMenu domain, Product product) {
    return DayMenuEntity.builder()
        .id(domain.getId())
        .product(product)
        .validFrom(domain.getValidFrom())
        .createdBy(domain.getCreatedBy())
        .build();
  }

  public DayMenuHistory toHistoryDomain(DayMenuHistoryEntity entity) {
    return DayMenuHistory.builder()
        .id(entity.getId())
        .productId(entity.getProduct().getId())
        .productName(entity.getProduct().getName())
        .productBasePrice(entity.getProduct().getBasePrice())
        .validFrom(entity.getValidFrom())
        .validUntil(entity.getValidUntil())
        .createdBy(entity.getCreatedBy())
        .build();
  }

  public DayMenuHistoryEntity toHistoryEntity(DayMenuHistory domain, Product product) {
    return DayMenuHistoryEntity.builder()
        .id(domain.getId())
        .product(product)
        .validFrom(domain.getValidFrom())
        .validUntil(domain.getValidUntil())
        .createdBy(domain.getCreatedBy())
        .build();
  }
}
