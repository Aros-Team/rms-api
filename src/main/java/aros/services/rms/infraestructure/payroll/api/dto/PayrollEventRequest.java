/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Request DTO for registering a payroll event. */
@Schema(description = "Request DTO for registering a payroll event")
public record PayrollEventRequest(
    @Schema(description = "User ID", example = "1") @NotNull(message = "User ID is required")
        Long userId,
    @Schema(description = "Date of the event", example = "2026-08-01")
        @NotNull(message = "Event date is required")
        LocalDate eventDate,
    @Schema(
            description =
                "Type of event (OVERTIME, NIGHT_SURCHARGE, ABSENCE,"
                    + " BONUS_ATTENDANCE, BONUS_PERFORMANCE, DEDUCTION)",
            example = "OVERTIME")
        @NotNull(message = "Event type is required")
        String eventType,
    @Schema(description = "Quantity (hours, units)", example = "2.5")
        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.01", message = "Quantity must be positive")
        BigDecimal quantity,
    @Schema(description = "Rate per unit", example = "25000")
        @NotNull(message = "Unit rate is required")
        @DecimalMin(value = "0.01", message = "Unit rate must be positive")
        BigDecimal unitRate,
    @Schema(description = "Optional notes", example = "Extra hours for weekend event")
        String notes) {}
