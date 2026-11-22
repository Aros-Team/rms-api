/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.in;

import aros.services.rms.core.analytics.domain.CohortReport;

/** Input port for the customer cohort report. */
public interface GetCohortUseCase {

  /**
   * Returns retention and ticket metrics for the requested bucket and range.
   *
   * @param bucket the time bucket (daily, weekly, monthly, yearly)
   * @param from the inclusive start period key
   * @param to the inclusive end period key
   * @return the cohort report
   */
  CohortReport execute(String bucket, String from, String to);
}
