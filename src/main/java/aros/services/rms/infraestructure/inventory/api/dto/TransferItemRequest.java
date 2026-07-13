/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Request DTO for a single transfer item. */
@Schema(description = "Single supply variant and quantity to transfer")
public record TransferItemRequest(
    @Schema(
            description = "Supply variant ID",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Supply variant ID is required")
        Long supplyVariantId,
    @Schema(
            description = "Quantity to transfer (must be > 0)",
            example = "5.000",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
        BigDecimal quantity) {}
