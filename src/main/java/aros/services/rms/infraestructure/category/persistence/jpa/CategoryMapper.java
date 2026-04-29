/* (C) 2026 */

package aros.services.rms.infraestructure.category.persistence.jpa;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.domain.OptionCategory;
import org.springframework.stereotype.Component;

/** Mapper between Category/OptionCategory domain models and their JPA entities. */
@Component
public class CategoryMapper {

  /** Converts a Category JPA entity to a domain model. */
  public Category toDomain(aros.services.rms.infraestructure.category.persistence.Category entity) {
    if (entity == null) {
      return null;
    }
    return Category.builder()
        .id(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        .enabled(entity.isEnabled())
        .build();
  }

  /** Converts a Category domain model to a JPA entity. */
  public aros.services.rms.infraestructure.category.persistence.Category toEntity(Category domain) {
    if (domain == null) {
      return null;
    }
    return aros.services.rms.infraestructure.category.persistence.Category.builder()
        .id(domain.getId())
        .name(domain.getName())
        .description(domain.getDescription())
        .enabled(domain.isEnabled())
        .build();
  }

  /** Converts an OptionCategory JPA entity to a domain model. */
  public OptionCategory toOptionCategoryDomain(
      aros.services.rms.infraestructure.category.persistence.OptionCategory entity) {
    if (entity == null) {
      return null;
    }
    return OptionCategory.builder()
        .id(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        .build();
  }

  /** Converts an OptionCategory domain model to a JPA entity. */
  public aros.services.rms.infraestructure.category.persistence.OptionCategory
      toOptionCategoryEntity(OptionCategory domain) {
    if (domain == null) {
      return null;
    }
    return aros.services.rms.infraestructure.category.persistence.OptionCategory.builder()
        .id(domain.getId())
        .name(domain.getName())
        .description(domain.getDescription())
        .build();
  }
}
