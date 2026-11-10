/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.api.dto;

import aros.services.rms.core.payroll.domain.Payroll;
import aros.services.rms.infraestructure.common.dto.MoneyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Response DTO for payroll data. */
@Schema(description = "Response DTO for payroll data")
public record PayrollResponse(
    @Schema(description = "Payroll ID", example = "1") Long id,
    @Schema(description = "User ID this payroll belongs to", example = "1") Long userId,
    @Schema(description = "User name", example = "Juan Perez") String userName,
    @Schema(description = "Period year", example = "2026") int year,
    @Schema(description = "Period month (1-12)", example = "7") int month,
    @Schema(description = "First day of the payroll period", example = "2026-07-01")
        LocalDate periodStart,
    @Schema(description = "Last day of the payroll period", example = "2026-07-31")
        LocalDate periodEnd,
    @Schema(description = "Base salary amount") MoneyDto baseSalary,
    @Schema(description = "Bonus amount") MoneyDto bonuses,
    @Schema(description = "Deductions amount") MoneyDto deductions,
    @Schema(description = "Net payable amount") MoneyDto netAmount,
    @Schema(description = "Total amount paid") MoneyDto paidAmount,
    @Schema(description = "Pending amount (netAmount - paidAmount)") MoneyDto pendingAmount,
    @Schema(description = "Total hours worked in the period", example = "192.0")
        BigDecimal hoursWorked,
    @Schema(description = "Current payroll status", example = "PENDING") String status,
    @Schema(description = "Optional notes", example = "Overtime included") String notes,
    @Schema(description = "User ID who registered this payroll", example = "1") Long registeredBy,
    @Schema(description = "Creation timestamp", example = "2026-07-15T10:30:00Z") Instant createdAt,
    @Schema(description = "Last update timestamp", example = "2026-07-15T12:00:00Z")
        Instant updatedAt) {

  /** Creates a PayrollResponse from a Payroll domain object. */
  public static PayrollResponse fromDomain(Payroll payroll) {
    if (payroll == null) {
      return null;
    }
    return new PayrollResponse(
        payroll.id(),
        payroll.userId(),
        null,
        payroll.period().getYear(),
        payroll.period().getMonthValue(),
        payroll.periodStart(),
        payroll.periodEnd(),
        payroll.baseSalary() != null ? MoneyDto.of(payroll.baseSalary()) : null,
        payroll.bonuses() != null ? MoneyDto.of(payroll.bonuses()) : null,
        payroll.deductions() != null ? MoneyDto.of(payroll.deductions()) : null,
        payroll.netAmount() != null ? MoneyDto.of(payroll.netAmount()) : null,
        payroll.paidAmount() != null ? MoneyDto.of(payroll.paidAmount()) : null,
        payroll.pendingAmount() != null ? MoneyDto.of(payroll.pendingAmount()) : null,
        payroll.hoursWorked(),
        payroll.status() != null ? payroll.status().name() : null,
        payroll.notes(),
        payroll.registeredBy(),
        payroll.createdAt(),
        payroll.updatedAt());
  }

  /** Creates a PayrollResponse from a Payroll domain object with a resolved user name. */
  public static PayrollResponse fromDomain(Payroll payroll, String userName) {
    PayrollResponse base = fromDomain(payroll);
    if (base == null) {
      return null;
    }
    return new PayrollResponse(
        base.id(),
        base.userId(),
        userName,
        base.year(),
        base.month(),
        base.periodStart(),
        base.periodEnd(),
        base.baseSalary(),
        base.bonuses(),
        base.deductions(),
        base.netAmount(),
        base.paidAmount(),
        base.pendingAmount(),
        base.hoursWorked(),
        base.status(),
        base.notes(),
        base.registeredBy(),
        base.createdAt(),
        base.updatedAt());
  }
}
