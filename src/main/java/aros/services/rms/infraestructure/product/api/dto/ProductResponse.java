/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.core.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for product data. */
@Schema(description = "Response DTO for product data")
public record ProductResponse(
    @Schema(description = "Product ID", example = "1") Long id,
    @Schema(description = "Product name", example = "Hamburguesa Clásica") String name,
    @Schema(description = "Product base price", example = "12.50") Double basePrice,
    @Schema(description = "Whether product is active", example = "true") boolean active,
    @Schema(description = "Category ID", example = "1") Long categoryId,
    @Schema(description = "Category name", example = "Hamburguesas") String categoryName,
    @Schema(description = "Preparation area ID", example = "1") Long areaId,
    @Schema(description = "Preparation area name", example = "Cocina") String areaName) {

  /**
   * Creates a response from a domain object.
   *
   * @param product the product
   * @return the response DTO
   */
  public static ProductResponse fromDomain(Product product) {
    if (product == null) {
      return null;
    }
    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getBasePrice(),
        product.isActive(),
        product.getCategory() != null ? product.getCategory().getId() : null,
        product.getCategory() != null ? product.getCategory().getName() : null,
        product.getPreparationAreaId(),
        product.getPreparationAreaName());
  }
}
