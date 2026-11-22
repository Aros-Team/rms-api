/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.infraestructure.common.dto.MoneyDto;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for one product in the top-selling ranking. */
@Schema(description = "Top-selling product")
public record TopSellingProductResponse(
    @Schema(description = "1-based rank", example = "1") int rank,
    @Schema(description = "Product id", example = "1") Long productId,
    @Schema(description = "Product name", example = "Hamburguesa Double Bacon Cheese")
        String productName,
    @Schema(description = "Units sold in the range", example = "4691") long unitsSold,
    @Schema(description = "Revenue in the range") MoneyDto revenue) {}
