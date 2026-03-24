/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.core.inventory.domain.ProductRecipe;
import aros.services.rms.infraestructure.inventory.persistence.ProductRecipeEntity;
import org.springframework.stereotype.Component;

/** Mapper between ProductRecipe domain model and ProductRecipeEntity JPA entity. */
@Component
public class ProductRecipeMapper {

  /** Converts a ProductRecipeEntity JPA entity to a domain model. */
  public ProductRecipe toDomain(ProductRecipeEntity entity) {
    if (entity == null) return null;
    return ProductRecipe.builder()
        .id(entity.getId())
        .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
        .supplyVariantId(
            entity.getSupplyVariant() != null ? entity.getSupplyVariant().getId() : null)
        .requiredQuantity(entity.getRequiredQuantity())
        .build();
  }

  /** Converts a ProductRecipe domain model to a JPA entity. */
  public ProductRecipeEntity toEntity(ProductRecipe domain) {
    if (domain == null) return null;
    return ProductRecipeEntity.builder()
        .id(domain.getId())
        .requiredQuantity(domain.getRequiredQuantity())
        .build();
  }
}
