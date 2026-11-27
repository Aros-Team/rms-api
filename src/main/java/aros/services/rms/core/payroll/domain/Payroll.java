/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.exception.PayrollAggregationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Payroll record for a user in a given month.
 *
 * <p>The {@code bonuses} and {@code deductions} totals are derived: they are the sum of the manual
 * component ({@code manualBonuses}, {@code manualDeductions}) and the event component recomputed
 * from the persisted payroll events belonging to the period. {@code paidAmount} is likewise
 * recomputed from the persisted settlements.
 *
 * @param id unique identifier
 * @param userId the user this payroll belongs to
 * @param period the year-month period
 * @param periodStart first day of the payroll period
 * @param periodEnd last day of the payroll period
 * @param baseSalary base salary amount
 * @param manualBonuses bonus amount entered by hand, before events
 * @param manualDeductions deduction amount entered by hand, before events
 * @param bonuses total bonus amount (manual + events)
 * @param deductions total deduction amount (manual + events)
 * @param netAmount net payable amount
 * @param paidAmount total amount paid
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
    Money manualBonuses,
    Money manualDeductions,
    Money bonuses,
    Money deductions,
    Money netAmount,
    Money paidAmount,
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
      throw new IllegalArgumentException("userId no puede ser nulo");
    }
    if (period == null) {
      throw new IllegalArgumentException("period no puede ser nulo");
    }
    if (baseSalary == null) {
      throw new IllegalArgumentException("baseSalary no puede ser nulo");
    }
    if (manualBonuses == null) {
      throw new IllegalArgumentException("manualBonuses no puede ser nulo");
    }
    if (manualDeductions == null) {
      throw new IllegalArgumentException("manualDeductions no puede ser nulo");
    }
    if (bonuses == null) {
      throw new IllegalArgumentException("bonuses no puede ser nulo");
    }
    if (deductions == null) {
      throw new IllegalArgumentException("deductions no puede ser nulo");
    }
    if (netAmount == null) {
      throw new IllegalArgumentException("netAmount no puede ser nulo");
    }
    if (paidAmount == null) {
      throw new IllegalArgumentException("paidAmount no puede ser nulo");
    }
    if (hoursWorked == null) {
      throw new IllegalArgumentException("hoursWorked no puede ser nulo");
    }
    if (status == null) {
      throw new IllegalArgumentException("status no puede ser nulo");
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

  /**
   * Returns whether the payroll can no longer be mutated.
   *
   * <p>PAID is terminal: events, settlements and manual totals are frozen once the payroll is paid.
   *
   * @return true when the payroll is PAID
   */
  public boolean isImmutable() {
    return isPaid();
  }

  /**
   * Returns whether a date falls inside the payroll period.
   *
   * @param date the date to check
   * @return true when the date is within {@code [periodStart, periodEnd]}
   */
  public boolean covers(LocalDate date) {
    return date != null && !date.isBefore(periodStart) && !date.isAfter(periodEnd);
  }

  /**
   * Returns the pending amount (netAmount minus paidAmount).
   *
   * @return the pending amount
   */
  public Money pendingAmount() {
    return netAmount.minus(paidAmount);
  }

  /**
   * Rebuilds the payroll with a new manual component, before events are folded in.
   *
   * <p>Used by the partial update use case: the caller supplies the manual bonuses/deductions and
   * the aggregation step then adds the persisted event totals on top.
   *
   * @param newBaseSalary the new base salary
   * @param newManualBonuses the new manual bonus component
   * @param newManualDeductions the new manual deduction component
   * @param newHoursWorked the new hours worked
   * @param newStatus the new status
   * @param newNotes the new notes
   * @param now the update timestamp
   * @return a payroll carrying the new manual component
   * @throws PayrollAggregationException when the manual component alone pushes the net below zero
   */
  public Payroll withManualTotals(
      Money newBaseSalary,
      Money newManualBonuses,
      Money newManualDeductions,
      BigDecimal newHoursWorked,
      PayrollStatus newStatus,
      String newNotes,
      Instant now) {
    Money provisionalNet = newBaseSalary.plus(newManualBonuses).minus(newManualDeductions);
    if (provisionalNet.isNegative()) {
      throw new PayrollAggregationException(
          "netAmount no puede ser negativo (base + bonus - deducciones < 0)");
    }
    return new Payroll(
        id,
        userId,
        period,
        periodStart,
        periodEnd,
        newBaseSalary,
        newManualBonuses,
        newManualDeductions,
        newManualBonuses,
        newManualDeductions,
        provisionalNet,
        paidAmount,
        newHoursWorked,
        newStatus,
        newNotes,
        registeredBy,
        createdAt,
        now);
  }

  /**
   * Applies a full re-aggregation of events and settlements.
   *
   * <p>This is the single place where the derived totals are computed. It is called with the
   * persisted event and settlement totals every time either source changes, so recomputing twice
   * with the same inputs is idempotent and deleting an event reverses exactly its own contribution.
   *
   * @param eventBonuses sum of the earning events in the period
   * @param eventDeductions sum of the deduction events in the period
   * @param newPaidAmount sum of the persisted settlements
   * @param now the update timestamp
   * @return the re-aggregated payroll
   * @throws PayrollAggregationException when the net is negative or falls below the paid amount
   */
  public Payroll reaggregate(
      Money eventBonuses, Money eventDeductions, Money newPaidAmount, Instant now) {
    Money newBonuses = manualBonuses.plus(eventBonuses);
    Money newDeductions = manualDeductions.plus(eventDeductions);
    Money newNet = baseSalary.plus(newBonuses).minus(newDeductions);
    if (newNet.isNegative()) {
      throw new PayrollAggregationException(
          "netAmount no puede ser negativo (base + bonus - deducciones < 0)");
    }
    if (newPaidAmount.isGreaterThan(newNet)) {
      throw new PayrollAggregationException(
          "paid amount "
              + newPaidAmount
              + " would exceed the payroll net amount "
              + newNet
              + "; void or reduce settlements first");
    }
    return new Payroll(
        id,
        userId,
        period,
        periodStart,
        periodEnd,
        baseSalary,
        manualBonuses,
        manualDeductions,
        newBonuses,
        newDeductions,
        newNet,
        newPaidAmount,
        hoursWorked,
        status,
        notes,
        registeredBy,
        createdAt,
        now);
  }
}
