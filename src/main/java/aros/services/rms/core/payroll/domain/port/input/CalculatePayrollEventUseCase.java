/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.input;

import aros.services.rms.core.payroll.domain.PayrollCalculationResult;

/** Use case for calculating the suggested unit rate for a payroll event. */
public interface CalculatePayrollEventUseCase {

  /**
   * Calculates the suggested unit rate and amount for a payroll event based on the user's hourly
   * rate and the event type multiplier.
   *
   * @param userId the user id
   * @param eventType the event type name (e.g. "OVERTIME")
   * @param quantity the quantity (hours)
   * @return the calculation result with hourly rate, multiplier, and suggested values
   */
  PayrollCalculationResult calculate(Long userId, String eventType, java.math.BigDecimal quantity);
}
