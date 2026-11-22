/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.in;

import aros.services.rms.core.analytics.domain.OperationsReport;

/** Input port for the operational efficiency report. */
public interface GetOperationsUseCase {

  /**
   * Returns RevPASH, covers, table turns and labor share for the requested bucket and range.
   *
   * @param bucket the time bucket (daily, weekly, monthly, yearly)
   * @param from the inclusive start period key
   * @param to the inclusive end period key
   * @return the operations report
   */
  OperationsReport execute(String bucket, String from, String to);
}
