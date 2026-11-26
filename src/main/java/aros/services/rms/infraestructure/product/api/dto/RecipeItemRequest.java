/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Request DTO for a recipe item. */
@Schema(
    description = "Request DTO for a recipe item (supply variant and required quantity)",
    example = "{\"supplyVariantId\": 1, \"requiredQuantity\": 250.0}")
public record RecipeItemRequest(
    @Schema(description = "Supply variant ID", example = "1")
        @NotNull(message = "El ID de variante de insumo es obligatorio")
        Long supplyVariantId,
    @Schema(description = "Required quantity", example = "250.0")
        @NotNull(message = "La cantidad requerida es obligatoria")
        @Positive(message = "La cantidad requerida debe ser positiva")
        BigDecimal requiredQuantity) {}
