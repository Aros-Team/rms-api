/* (C) 2026 */
package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.core.product.domain.ProductOption;

/**
 * Response DTO for product option data.
 *
 * @param id the option id
 * @param name the option name
 * @param optionCategoryId the option category id
 * @param optionCategoryName the option category name
 */
public record ProductOptionResponse(
    Long id, String name, Long optionCategoryId, String optionCategoryName) {

  /** Converts a domain ProductOption to a ProductOptionResponse DTO. */
  public static ProductOptionResponse fromDomain(ProductOption option) {
    if (option == null) return null;
    return new ProductOptionResponse(
        option.getId(),
        option.getName(),
        option.getCategory() != null ? option.getCategory().getId() : null,
        option.getCategory() != null ? option.getCategory().getName() : null);
  }
}