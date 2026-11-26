/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/** Request DTO to update the per-product surcharge of an associated option. */
@Schema(description = "Update the per-product surcharge of an associated option")
public record OptionExtraPriceRequest(
    @Schema(description = "New surcharge for this product", example = "2500.00")
        BigDecimal extraPrice) {}
