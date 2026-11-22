/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.in;

import aros.services.rms.core.analytics.domain.AlertSeverity;
import aros.services.rms.core.analytics.domain.AlertStatus;
import aros.services.rms.core.analytics.domain.AlertsPage;

/** Input port for paged alert reads. */
public interface GetAlertsUseCase {

  /**
   * Returns a page of alerts filtered by status, severity and period.
   *
   * @param status optional status filter
   * @param severity optional severity filter
   * @param bucket optional period bucket filter
   * @param from optional inclusive period key
   * @param to optional inclusive period key
   * @param limit page size (1..100)
   * @param offset zero-based offset
   * @return the alert page
   */
  AlertsPage execute(
      AlertStatus status,
      AlertSeverity severity,
      String bucket,
      String from,
      String to,
      int limit,
      int offset);
}
