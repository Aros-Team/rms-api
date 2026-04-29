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
 * 409. Otherwise a new variant is created with stock initialized to zero.
 */
@Schema(
    description = "Request payload for creating a new supply variant (presentación física)",
    example = "{\"supplyId\": 3, \"unitId\": 2, \"quantity\": 0.500}")
public record CreateSupplyVariantRequest(
    @Schema(description = "Supply (insumo) ID — must already exist", example = "3")
        @NotNull(message = "supplyId is required") Long supplyId,
    @Schema(description = "Unit of measure ID — get from GET /api/v1/supplies/units", example = "2")
        @NotNull(message = "unitId is required") Long unitId,
    @Schema(description = "Quantity per unit presentation (e.g. 0.500 for 500g)", example = "0.500")
        @NotNull(message = "quantity is required") @DecimalMin(value = "0.001", message = "quantity must be greater than 0") BigDecimal quantity) {}
