/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Request DTO for a single transfer item. */
public record TransferItemRequest(
    @NotNull(message = "Supply variant ID is required") Long supplyVariantId,
    @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
        BigDecimal quantity) {}
