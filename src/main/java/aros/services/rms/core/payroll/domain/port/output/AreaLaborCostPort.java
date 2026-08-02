/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.output;

import aros.services.rms.core.common.money.domain.Money;
import java.time.YearMonth;

/**
 * Calculates the labor cost per hour for a given area.
 *
 * <p>Modes:
 *
 * <ul>
 *   <li>REAL: uses payroll data (net_amount / hours_worked) for the period
 *   <li>STANDARD: uses sum(salary) / sum(expected_hours_per_month) for active workers in area
 *   <li>AUTO: REAL if payroll exists, otherwise STANDARD
 * </ul>
 */
public interface AreaLaborCostPort {

  /**
   * Calculates the cost per hour for the given area and period.
   *
   * @param areaId the area id
   * @param period the year-month period
   * @return the cost per hour
   */
  Money calculateCostPerHour(Long areaId, YearMonth period);
}
