/* (C) 2026 */
package aros.services.rms.infraestructure.product.persistence.jpa;

import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.infraestructure.area.persistence.jpa.Area;
import aros.services.rms.infraestructure.category.persistence.jpa.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Mapper between Product/ProductOption domain models and their JPA entities. */
@Component
@RequiredArgsConstructor
public class ProductMapper {

  private final CategoryMapper categoryMapper;

  /** Converts a Product JPA entity to a domain model. */
  public Product toProductDomain(
      aros.services.rms.infraestructure.product.persistence.Product entity) {
    if (entity == null) return null;

    // Note: optionIds are not loaded here to avoid LazyInitializationException.
    // They are only populated when explicitly needed (e.g., after association operations).
    return Product.builder()
        .id(entity.getId())
        .name(entity.getName())
        .basePrice(entity.getBasePrice())
        .hasOptions(entity.isHasOptions())
        .active(entity.isActive())
        .category(categoryMapper.toDomain(entity.getCategory()))
        .preparationAreaId(
            entity.getPreparationArea() != null ? entity.getPreparationArea().getId() : null)
        .preparationAreaName(
            entity.getPreparationArea() != null ? entity.getPreparationArea().getName() : null)
        .build();
  }

  /** Converts a ProductOption JPA entity to a domain model. */
  public ProductOption toProductOptionDomain(
      aros.services.rms.infraestructure.product.persistence.ProductOption entity) {
    if (entity == null) return null;
    return ProductOption.builder()
        .id(entity.getId())
        .name(entity.getName())
        .category(categoryMapper.toOptionCategoryDomain(entity.getCategory()))
        .build();
  }

  /** Converts a Product domain model to a JPA entity. */
  public aros.services.rms.infraestructure.product.persistence.Product toProductEntity(
      Product domain) {
    if (domain == null) return null;
    aros.services.rms.infraestructure.product.persistence.Product entity =
        aros.services.rms.infraestructure.product.persistence.Product.builder()
            .id(domain.getId())
            .name(domain.getName())
            .basePrice(domain.getBasePrice())
            .hasOptions(domain.isHasOptions())
            .active(domain.isActive())
            .category(categoryMapper.toEntity(domain.getCategory()))
            .build();

    if (domain.getPreparationAreaId() != null) {
      entity.setPreparationArea(Area.builder().id(domain.getPreparationAreaId()).build());
    }

    return entity;
  }

  /** Converts a ProductOption domain model to a JPA entity. */
  public aros.services.rms.infraestructure.product.persistence.ProductOption toProductOptionEntity(
      ProductOption domain) {
    if (domain == null) return null;
    return aros.services.rms.infraestructure.product.persistence.ProductOption.builder()
        .id(domain.getId())
        .name(domain.getName())
        .category(categoryMapper.toOptionCategoryEntity(domain.getCategory()))
        .build();
  }
}
