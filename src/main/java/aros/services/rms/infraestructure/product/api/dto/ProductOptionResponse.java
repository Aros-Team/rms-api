/* (C) 2026 */
package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.core.product.domain.ProductOption;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for product option data. */
@Schema(description = "Response DTO para datos de opción de producto")
public record ProductOptionResponse(
    @Schema(description = "ID de la opción", example = "1") Long id,
    @Schema(description = "Nombre de la opción", example = "Grande (1.5L)") String name,
    @Schema(description = "ID de la categoría de opción", example = "1") Long optionCategoryId,
    @Schema(description = "Nombre de la categoría de opción", example = "Tamaños")
        String optionCategoryName) {

  /**
   * Creates a response from a domain object.
   *
   * @param option the product option
   * @return the response DTO
   */
  public static ProductOptionResponse fromDomain(ProductOption option) {
    if (option == null) return null;
    return new ProductOptionResponse(
        option.getId(),
        option.getName(),
        option.getCategory() != null ? option.getCategory().getId() : null,
        option.getCategory() != null ? option.getCategory().getName() : null);
  }
}
