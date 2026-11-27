/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Request DTO for creating or updating a payroll record. */
@Schema(
    description = "Request DTO for creating or updating a payroll record",
    example =
        "{\"userId\": 1, \"year\": 2026, \"month\": 7, \"periodStart\": \"2026-07-01\", "
            + "\"periodEnd\": \"2026-07-31\", \"baseSalary\": 2500000.00, \"bonuses\": 200000.00, "
            + "\"deductions\": 150000.00, \"hoursWorked\": 192.0}")
public record PayrollRequest(
    @Schema(description = "User ID this payroll belongs to", example = "1")
        @NotNull(message = "El ID de usuario es obligatorio")
        Long userId,
    @Schema(description = "Period year", example = "2026")
        @NotNull(message = "El año del periodo es obligatorio")
        Integer year,
    @Schema(description = "Period month (1-12)", example = "7")
        @NotNull(message = "El mes del periodo es obligatorio")
        Integer month,
    @Schema(description = "First day of the payroll period", example = "2026-07-01")
        @NotNull(message = "El inicio del periodo es obligatorio")
        LocalDate periodStart,
    @Schema(description = "Last day of the payroll period", example = "2026-07-31")
        @NotNull(message = "El fin del periodo es obligatorio")
        LocalDate periodEnd,
    @Schema(description = "Base salary amount", example = "2500000.00")
        @NotNull(message = "El salario base es obligatorio")
        @Positive(message = "El salario base debe ser positivo")
        BigDecimal baseSalary,
    @Schema(description = "Bonus amount (default 0)", example = "200000.00") BigDecimal bonuses,
    @Schema(description = "Deductions amount (default 0)", example = "150000.00")
        BigDecimal deductions,
    @Schema(description = "Total hours worked in the period", example = "192.0")
        @NotNull(message = "Las horas trabajadas son obligatorias")
        @Positive(message = "Las horas trabajadas deben ser positivas")
        BigDecimal hoursWorked,
    @Schema(description = "Optional notes", example = "Overtime included") String notes) {}
