/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Payroll record for a user in a given month.
 *
 * @param id unique identifier
 * @param userId the user this payroll belongs to
 * @param period the year-month period
 * @param periodStart first day of the payroll period
 * @param periodEnd last day of the payroll period
 * @param baseSalary base salary amount
 * @param bonuses bonus amount
 * @param deductions deductions amount
 * @param netAmount net payable amount
 * @param hoursWorked total hours worked in the period
 * @param status current payroll status
 * @param notes optional notes
 * @param registeredBy user id who registered this payroll
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
public record Payroll(
    Long id,
    Long userId,
    YearMonth period,
    LocalDate periodStart,
    LocalDate periodEnd,
    Money baseSalary,
    Money bonuses,
    Money deductions,
    Money netAmount,
    BigDecimal hoursWorked,
    PayrollStatus status,
    String notes,
    Long registeredBy,
    Instant createdAt,
    Instant updatedAt) {
  /**
   * Canonical constructor with validation.
   *
   * @throws IllegalArgumentException if any required field is null
   */
  public Payroll {
    if (userId == null) {
      throw new IllegalArgumentException("userId must not be null");
    }
    if (period == null) {
      throw new IllegalArgumentException("period must not be null");
    }
    if (baseSalary == null) {
      throw new IllegalArgumentException("baseSalary must not be null");
    }
    if (bonuses == null) {
      throw new IllegalArgumentException("bonuses must not be null");
    }
    if (deductions == null) {
      throw new IllegalArgumentException("deductions must not be null");
    }
    if (netAmount == null) {
      throw new IllegalArgumentException("netAmount must not be null");
    }
    if (hoursWorked == null) {
      throw new IllegalArgumentException("hoursWorked must not be null");
    }
    if (status == null) {
      throw new IllegalArgumentException("status must not be null");
    }
  }

  /**
   * Returns whether this payroll is in PENDING status.
   *
   * @return true if pending
   */
  public boolean isPending() {
    return status == PayrollStatus.PENDING;
  }

  /**
   * Returns whether this payroll is in PAID status.
   *
   * @return true if paid
   */
  public boolean isPaid() {
    return status == PayrollStatus.PAID;
  }
}
