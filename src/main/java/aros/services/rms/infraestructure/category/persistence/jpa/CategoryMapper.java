/* (C) 2026 */

package aros.services.rms.infraestructure.category.persistence.jpa;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.domain.OptionGroup;
import aros.services.rms.core.category.domain.OptionSelectionType;
import org.springframework.stereotype.Component;

/** Mapper between Category/OptionGroup domain models and their JPA entities. */
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

  /** Converts an OptionGroup JPA entity to a domain model. */
  public OptionGroup toOptionGroupDomain(
      aros.services.rms.infraestructure.category.persistence.OptionGroup entity) {
    if (entity == null) {
      return null;
    }
    return OptionGroup.builder()
        .id(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        .selectionType(toSelectionType(entity.getSelectionType()))
        .replaceSupplyCategoryId(entity.getReplaceSupplyCategoryId())
        .build();
  }

  /** Converts an OptionGroup domain model to a JPA entity. */
  public aros.services.rms.infraestructure.category.persistence.OptionGroup toOptionGroupEntity(
      OptionGroup domain) {
    if (domain == null) {
      return null;
    }
    return aros.services.rms.infraestructure.category.persistence.OptionGroup.builder()
        .id(domain.getId())
        .name(domain.getName())
        .description(domain.getDescription())
        .selectionType(toSelectionType(domain.getSelectionType()))
        .replaceSupplyCategoryId(domain.getReplaceSupplyCategoryId())
        .build();
  }

  private static OptionSelectionType toSelectionType(
      aros.services.rms.infraestructure.category.persistence.OptionGroup entity) {
    OptionSelectionType stored = entity == null ? null : entity.getSelectionType();
    return toSelectionType(stored);
  }

  private static OptionSelectionType toSelectionType(OptionSelectionType stored) {
    if (stored == null) {
      return OptionSelectionType.SINGLE_CHOICE;
    }
    try {
      return OptionSelectionType.valueOf(stored.name());
    } catch (IllegalArgumentException unknown) {
      return OptionSelectionType.SINGLE_CHOICE;
    }
  }
}
