/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.in;

import aros.services.rms.core.analytics.domain.PrimeCostReport;

/** Input port for retrieving prime cost & margins data over a time range. */
public interface GetPrimeCostUseCase {

  /**
   * Returns a prime cost & margins report for the requested time bucket and period range.
   *
   * @param bucket the time bucket (daily, weekly, monthly, yearly)
   * @param from the inclusive start period key
   * @param to the inclusive end period key
   * @return the prime cost report
   */
  PrimeCostReport execute(String bucket, String from, String to);
}
