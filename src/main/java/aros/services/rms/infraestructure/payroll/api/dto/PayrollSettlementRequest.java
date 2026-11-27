/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Request DTO for registering a payroll settlement. */
@Schema(description = "Request DTO for registering a payroll settlement")
public record PayrollSettlementRequest(
    @Schema(description = "Payroll ID", example = "1")
        @NotNull(message = "El ID de nómina es obligatorio")
        Long payrollId,
    @Schema(description = "User ID", example = "1")
        @NotNull(message = "El ID de usuario es obligatorio")
        Long userId,
    @Schema(
            description = "Type of settlement (DAILY, WEEKLY, BIWEEKLY, MONTHLY)",
            example = "MONTHLY")
        @NotNull(message = "El tipo de liquidación es obligatorio")
        String settlementType,
    @Schema(description = "Period start date", example = "2026-08-01")
        @NotNull(message = "El inicio del periodo es obligatorio")
        LocalDate periodStart,
    @Schema(description = "Period end date", example = "2026-08-31")
        @NotNull(message = "El fin del periodo es obligatorio")
        LocalDate periodEnd,
    @Schema(description = "Settlement amount", example = "2500000")
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser positivo")
        BigDecimal amount,
    @Schema(description = "Optional notes", example = "August salary payment") String notes) {}
