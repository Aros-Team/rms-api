/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.core.product.domain.ProductOption;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for product option data. */
@Schema(description = "Response DTO for product option data")
public record ProductOptionResponse(
    @Schema(description = "Option ID", example = "1") Long id,
    @Schema(description = "Option name", example = "Large (1.5L)") String name,
    @Schema(description = "Option category ID", example = "1") Long optionCategoryId,
    @Schema(description = "Option category name", example = "Sizes") String optionCategoryName) {

  /**
   * Creates a response from a domain object.
   *
   * @param option the product option
   * @return the response DTO
   */
  public static ProductOptionResponse fromDomain(ProductOption option) {
    if (option == null) {
      return null;
    }
    return new ProductOptionResponse(
        option.getId(),
        option.getName(),
        option.getCategory() != null ? option.getCategory().getId() : null,
        option.getCategory() != null ? option.getCategory().getName() : null);
  }
}
