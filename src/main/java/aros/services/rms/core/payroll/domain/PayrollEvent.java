/* (C) 2026 */

package aros.services.rms.core.payroll.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A payroll event representing a single bonus, deduction, or hours-based entry for a user.
 *
 * @param id unique identifier
 * @param userId the user this event belongs to
 * @param eventDate the date of the event
 * @param eventType the type of event
 * @param quantity the quantity (hours for hours-based, unit count for others)
 * @param unitRate the rate per unit
 * @param amount the computed amount (quantity x unitRate)
 * @param notes optional notes
 * @param createdAt creation timestamp
 * @param createdBy user id who created this event
 */
public record PayrollEvent(
    Long id,
    Long userId,
    LocalDate eventDate,
    PayrollEventType eventType,
    BigDecimal quantity,
    BigDecimal unitRate,
    BigDecimal amount,
    String notes,
    Instant createdAt,
    String createdBy) {

  /**
   * Creates a new PayrollEvent with computed amount.
   *
   * @param userId the user id
   * @param eventDate the event date
   * @param eventType the event type
   * @param quantity the quantity
   * @param unitRate the unit rate
   * @param notes optional notes
   * @param createdBy the user who created this event
   * @return a new PayrollEvent with amount = quantity x unitRate
   */
  public static PayrollEvent create(
      Long userId,
      LocalDate eventDate,
      PayrollEventType eventType,
      BigDecimal quantity,
      BigDecimal unitRate,
      String notes,
      String createdBy) {
    BigDecimal amount = quantity.multiply(unitRate);
    return new PayrollEvent(
        null, userId, eventDate, eventType, quantity, unitRate, amount, notes, null, createdBy);
  }
}
