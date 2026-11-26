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
    @Schema(description = "User ID", example = "1")
        @NotNull(message = "El ID de usuario es obligatorio")
        Long userId,
    @Schema(description = "Date of the event", example = "2026-08-01")
        @NotNull(message = "La fecha del evento es obligatoria")
        LocalDate eventDate,
    @Schema(
            description =
                "Type of event (OVERTIME, NIGHT_SURCHARGE, ABSENCE,"
                    + " BONUS_ATTENDANCE, BONUS_PERFORMANCE, DEDUCTION)",
            example = "OVERTIME")
        @NotNull(message = "El tipo de evento es obligatorio")
        String eventType,
    @Schema(description = "Quantity (hours, units)", example = "2.5")
        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser positiva")
        BigDecimal quantity,
    @Schema(description = "Rate per unit", example = "25000")
        @NotNull(message = "La tarifa unitaria es obligatoria")
        @DecimalMin(value = "0.01", message = "La tarifa unitaria debe ser positiva")
        BigDecimal unitRate,
    @Schema(description = "Optional notes", example = "Extra hours for weekend event")
        String notes) {}
