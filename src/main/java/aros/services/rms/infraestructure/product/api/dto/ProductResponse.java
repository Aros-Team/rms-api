/* (C) 2026 */
package aros.services.rms.infraestructure.product.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import aros.services.rms.core.product.domain.Product;

@Schema(description = "Response DTO for product data")
public record ProductResponse(
    @Schema(description = "Product ID", example = "1") Long id,
    @Schema(description = "Product name", example = "Hamburguesa Clásica") String name,
    @Schema(description = "Product base price", example = "12.50") Double basePrice,
    @Schema(description = "Whether product supports options", example = "true") boolean hasOptions,
    @Schema(description = "Whether product is active", example = "true") boolean active,
    @Schema(description = "Category ID", example = "1") Long categoryId,
    @Schema(description = "Category name", example = "Hamburguesas") String categoryName,
    @Schema(description = "Preparation area ID", example = "1") Long areaId) {

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