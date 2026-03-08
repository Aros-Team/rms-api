/* (C) 2026 */
package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.core.product.domain.Product;

/**
 * Response DTO for product data.
 *
 * @param id the product id
 * @param name the product name
 * @param basePrice the product base price
 * @param hasOptions whether the product supports options
 * @param active whether the product is active
 * @param categoryId the category id
 * @param categoryName the category name
 * @param areaId the preparation area id
 */
public record ProductResponse(
    Long id,
    String name,
    Double basePrice,
    boolean hasOptions,
    boolean active,
    Long categoryId,
    String categoryName,
    Long areaId) {

  /** Converts a domain Product to a ProductResponse DTO. */
  public static ProductResponse fromDomain(Product product) {
    if (product == null) return null;
    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getBasePrice(),
        product.isHasOptions(),
        product.isActive(),
        product.getCategory() != null ? product.getCategory().getId() : null,
        product.getCategory() != null ? product.getCategory().getName() : null,
        product.getPreparationAreaId());
  }
}