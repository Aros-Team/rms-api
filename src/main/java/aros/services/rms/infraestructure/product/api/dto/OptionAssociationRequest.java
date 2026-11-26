/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Request DTO to associate an existing product option with a product. */
@Schema(description = "Associate an option with a product and set its per-product surcharge")
public record OptionAssociationRequest(
    @Schema(description = "Option ID", example = "12")
        @NotNull(message = "El ID de la opción es obligatorio")
        Long optionId,
    @Schema(description = "Surcharge applied for this product", example = "2500.00")
        BigDecimal extraPrice) {}
