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
    @Schema(description = "Payroll ID", example = "1") @NotNull(message = "Payroll ID is required")
        Long payrollId,
    @Schema(description = "User ID", example = "1") @NotNull(message = "User ID is required")
        Long userId,
    @Schema(
            description = "Type of settlement (DAILY, WEEKLY, BIWEEKLY, MONTHLY)",
            example = "MONTHLY")
        @NotNull(message = "Settlement type is required")
        String settlementType,
    @Schema(description = "Period start date", example = "2026-08-01")
        @NotNull(message = "Period start is required")
        LocalDate periodStart,
    @Schema(description = "Period end date", example = "2026-08-31")
        @NotNull(message = "Period end is required")
        LocalDate periodEnd,
    @Schema(description = "Settlement amount", example = "2500000")
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        BigDecimal amount,
    @Schema(description = "Optional notes", example = "August salary payment") String notes) {}
