/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A payroll settlement representing a payment made against a payroll record.
 *
 * @param id unique identifier
 * @param payrollId the payroll this settlement is associated with
 * @param userId the user this settlement belongs to
 * @param settlementType the type of settlement period
 * @param periodStart start of the settlement period
 * @param periodEnd end of the settlement period
 * @param amount the settlement amount
 * @param notes optional notes
 * @param settledAt timestamp when the settlement was made
 * @param settledBy user id who settled this payroll
 */
public record PayrollSettlement(
    Long id,
    Long payrollId,
    Long userId,
    SettlementType settlementType,
    LocalDate periodStart,
    LocalDate periodEnd,
    Money amount,
    String notes,
    Instant settledAt,
    String settledBy) {

  /**
   * Creates a new PayrollSettlement.
   *
   * @param payrollId the payroll id
   * @param userId the user id
   * @param settlementType the settlement type
   * @param periodStart the period start
   * @param periodEnd the period end
   * @param amount the amount
   * @param notes optional notes
   * @param settledBy the user who settled
   * @return a new PayrollSettlement
   */
  public static PayrollSettlement create(
      Long payrollId,
      Long userId,
      SettlementType settlementType,
      LocalDate periodStart,
      LocalDate periodEnd,
      Money amount,
      String notes,
      String settledBy) {
    return new PayrollSettlement(
        null,
        payrollId,
        userId,
        settlementType,
        periodStart,
        periodEnd,
        amount,
        notes,
        null,
        settledBy);
  }

  /**
   * Returns whether this settlement is positive.
   *
   * @return true when the amount is greater than zero
   */
  public boolean isPositive() {
    return amount.isPositive();
  }

  /**
   * Returns whether the settlement dates fall inside the payroll period.
   *
   * @param payroll the payroll to compare against
   * @return true when both dates are inside the payroll period and ordered correctly
   */
  public boolean fallsWithin(Payroll payroll) {
    if (periodStart == null || periodEnd == null || periodEnd.isBefore(periodStart)) {
      return false;
    }
    return !periodStart.isBefore(payroll.periodStart()) && !periodEnd.isAfter(payroll.periodEnd());
  }

  /**
   * Returns whether this settlement period overlaps another settlement period.
   *
   * <p>Settlements of the same payroll may not overlap, which also makes a repeated identical
   * payment for the same period a rejected duplicate instead of a double count.
   *
   * @param other the other settlement
   * @return true when the two date ranges intersect
   */
  public boolean overlaps(PayrollSettlement other) {
    return !periodEnd.isBefore(other.periodStart()) && !other.periodEnd().isBefore(periodStart);
  }
}
