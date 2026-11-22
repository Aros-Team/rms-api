/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.out;

import aros.services.rms.core.analytics.domain.AlertSeverity;
import aros.services.rms.core.analytics.domain.AlertStatus;
import aros.services.rms.core.analytics.domain.SystemAlert;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Output port for system_alerts persistence. */
public interface SystemAlertRepositoryPort {

  /**
   * Finds alerts matching the filters, newest first.
   *
   * @param status optional status filter
   * @param severity optional severity filter
   * @param bucket optional period bucket filter
   * @param from optional inclusive period key
   * @param to optional inclusive period key
   * @param limit page size
   * @param offset zero-based offset
   * @return the matching alerts
   */
  List<SystemAlert> find(
      AlertStatus status,
      AlertSeverity severity,
      String bucket,
      String from,
      String to,
      int limit,
      int offset);

  /**
   * Counts alerts matching the filters.
   *
   * @param status optional status filter
   * @param severity optional severity filter
   * @param bucket optional period bucket filter
   * @param from optional inclusive period key
   * @param to optional inclusive period key
   * @return the total number of matches
   */
  long count(AlertStatus status, AlertSeverity severity, String bucket, String from, String to);

  /**
   * Finds an alert by id.
   *
   * @param id the alert id
   * @return the alert, if present
   */
  Optional<SystemAlert> findById(Long id);

  /**
   * Marks an alert read.
   *
   * @param id the alert id
   * @param readAt the read timestamp
   */
  void markRead(Long id, Instant readAt);

  /**
   * Inserts an alert only when no alert with the same {@code (type, metric, periodKey)} exists.
   *
   * @param alert the candidate alert (id ignored)
   * @return true when a row was inserted
   */
  boolean insertIfAbsent(SystemAlert alert);

  /**
   * Counts all persisted alerts regardless of status.
   *
   * @return the total alert count
   */
  long countAll();
}
