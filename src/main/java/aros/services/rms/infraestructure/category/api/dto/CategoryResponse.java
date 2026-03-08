/* (C) 2026 */
package aros.services.rms.infraestructure.category.api.dto;

import aros.services.rms.core.category.domain.Category;

/**
 * Response DTO for category data.
 *
 * @param id the category id
 * @param name the category name
 * @param description the category description
 * @param enabled whether the category is enabled
 */
public record CategoryResponse(Long id, String name, String description, boolean enabled) {

  /** Converts a domain Category to a CategoryResponse DTO. */
  public static CategoryResponse fromDomain(Category category) {
    if (category == null) return null;
    return new CategoryResponse(
        category.getId(), category.getName(), category.getDescription(), category.isEnabled());
  }
}