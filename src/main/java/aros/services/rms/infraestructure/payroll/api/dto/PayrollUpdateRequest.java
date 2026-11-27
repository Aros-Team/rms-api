/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * Partial update payload for `PATCH /api/v1/payroll/{id}`.
 *
 * <p>Every field is optional: omitted fields keep their current value. Identity and period fields
 * ({@code userId}, {@code year}, {@code month}, dates) are intentionally absent because a payroll
 * cannot be moved to another worker or period through this endpoint.
 */
@Schema(description = "Partial update for an existing payroll record")
public record PayrollUpdateRequest(
    @Schema(description = "Base salary amount", example = "2500000.00")
        @DecimalMin(value = "0.00", message = "El salario base debe ser >= 0")
        BigDecimal baseSalary,
    @Schema(description = "Bonus amount", example = "200000.00") BigDecimal bonuses,
    @Schema(description = "Deductions amount", example = "150000.00") BigDecimal deductions,
    @Schema(description = "Total hours worked in the period", example = "192.0")
        @DecimalMin(value = "0.00", message = "Las horas trabajadas deben ser >= 0")
        BigDecimal hoursWorked,
    @Schema(description = "Payroll status (PENDING, ACCRUED, PAID)", example = "ACCRUED")
        String status,
    @Schema(description = "Optional notes", example = "Overtime included") String notes) {}
