/* (C) 2026 */

package aros.services.rms.infraestructure.daymenu.persistence;

import aros.services.rms.core.daymenu.domain.DayMenu;
import aros.services.rms.core.daymenu.domain.DayMenuHistory;
import aros.services.rms.infraestructure.product.persistence.Product;
import org.springframework.stereotype.Component;

/** Maps between DayMenu/DayMenuHistory domain models and JPA entities. */
@Component
public class DayMenuMapper {

  /**
   * Converts an entity to domain.
   *
   * @param entity the entity
   * @return the domain
   */
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

  /**
   * Converts a domain to entity.
   *
   * @param domain the domain
   * @param product the product entity
   * @return the entity
   */
  public DayMenuEntity toEntity(DayMenu domain, Product product) {
    return DayMenuEntity.builder()
        .id(domain.getId())
        .product(product)
        .validFrom(domain.getValidFrom())
        .createdBy(domain.getCreatedBy())
        .build();
  }

  /**
   * Converts a history entity to domain.
   *
   * @param entity the entity
   * @return the domain
   */
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

  /**
   * Converts a history domain to entity.
   *
   * @param domain the domain
   * @param product the product entity
   * @return the entity
   */
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
