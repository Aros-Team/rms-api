/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

import java.math.BigDecimal;
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
    BigDecimal amount,
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
      BigDecimal amount,
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
}
