/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.in;

import aros.services.rms.core.analytics.domain.MenuEngineeringReport;

/** Input port for retrieving the menu engineering BCG report over a time range. */
public interface GetMenuEngineeringUseCase {

  /**
   * Returns a menu engineering BCG report for the requested time bucket and period range.
   *
   * @param bucket the time bucket (daily, weekly, monthly, yearly)
   * @param from the inclusive start period key
   * @param to the inclusive end period key
   * @param categoryId optional category filter (null for all)
   * @return the menu engineering report
   */
  MenuEngineeringReport execute(String bucket, String from, String to, Long categoryId);
}
