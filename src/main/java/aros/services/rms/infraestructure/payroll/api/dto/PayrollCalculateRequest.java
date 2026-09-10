/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Request DTO for calculating the suggested unit rate for a payroll event. */
@Schema(description = "Request to calculate suggested rate for a payroll event")
public record PayrollCalculateRequest(
    @Schema(description = "User ID", example = "1") @NotNull(message = "User ID is required")
        Long userId,
    @Schema(
            description =
                "Type of event (OVERTIME, NIGHT_SURCHARGE, ABSENCE, BONUS_ATTENDANCE,"
                    + " BONUS_PERFORMANCE, DEDUCTION)",
            example = "OVERTIME")
        @NotNull(message = "Event type is required")
        String eventType,
    @Schema(description = "Quantity (hours)", example = "2.5")
        @DecimalMin(value = "0.01", message = "Quantity must be positive")
        BigDecimal quantity) {}
