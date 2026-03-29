/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.core.inventory.domain.OptionRecipe;
import aros.services.rms.infraestructure.inventory.persistence.OptionRecipeEntity;
import org.springframework.stereotype.Component;

/** Mapper between OptionRecipe domain model and OptionRecipeEntity JPA entity. */
@Component
public class OptionRecipeMapper {

  /** Converts an OptionRecipeEntity JPA entity to a domain model. */
  public OptionRecipe toDomain(OptionRecipeEntity entity) {
    if (entity == null) return null;
    return OptionRecipe.builder()
        .id(entity.getId())
        .optionId(entity.getOption() != null ? entity.getOption().getId() : null)
        .supplyVariantId(
            entity.getSupplyVariant() != null ? entity.getSupplyVariant().getId() : null)
        .requiredQuantity(entity.getRequiredQuantity())
        .build();
  }

  /** Converts an OptionRecipe domain model to a JPA entity. */
  public OptionRecipeEntity toEntity(OptionRecipe domain) {
    if (domain == null) return null;
    return OptionRecipeEntity.builder()
        .id(domain.getId())
        .requiredQuantity(domain.getRequiredQuantity())
        .build();
  }
}
