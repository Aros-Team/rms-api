/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for creating a new supply variant.
 *
 * <p>If a variant with the same supplyId + unitId + quantity already exists, the server returns
 * 409. Otherwise a new variant is created with stock initialized to zero and, when supplied, the
 * given unit cost (used by the product/suggested-price cost engine).
 */
@Schema(
    description = "Request payload for creating a new supply variant (physical presentation)",
    example = "{\"supplyId\": 3, \"unitId\": 2, \"quantity\": 0.500, \"unitCost\": 12000.00}")
public record CreateSupplyVariantRequest(
    @Schema(description = "Supply (insumo) ID — must already exist", example = "3")
        @NotNull(message = "supplyId is required")
        Long supplyId,
    @Schema(description = "Unit of measure ID — get from GET /api/v1/supplies/units", example = "2")
        @NotNull(message = "unitId is required")
        Long unitId,
    @Schema(description = "Quantity per unit presentation (e.g. 0.500 for 500g)", example = "0.500")
        @NotNull(message = "quantity is required")
        @DecimalMin(value = "0.001", message = "quantity must be greater than 0")
        BigDecimal quantity,
    @Schema(
            description =
                "Optional unit cost of this presentation, in the system currency"
                    + " (used by the product cost / suggested price engine)",
            example = "12000.00",
            nullable = true)
        @DecimalMin(value = "0.00", inclusive = true, message = "unitCost must be >= 0")
        BigDecimal unitCost) {}
