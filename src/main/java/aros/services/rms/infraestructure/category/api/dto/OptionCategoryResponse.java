/* (C) 2026 */
package aros.services.rms.infraestructure.category.api.dto;

import aros.services.rms.core.category.domain.OptionCategory;

/**
 * Response DTO for option category data.
 *
 * @param id the option category id
 * @param name the option category name
 * @param description the option category description
 */
public record OptionCategoryResponse(Long id, String name, String description) {

  /** Converts a domain OptionCategory to an OptionCategoryResponse DTO. */
  public static OptionCategoryResponse fromDomain(OptionCategory optionCategory) {
    if (optionCategory == null) return null;
    return new OptionCategoryResponse(
        optionCategory.getId(), optionCategory.getName(), optionCategory.getDescription());
  }
}