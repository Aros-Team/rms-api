/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.in;

/** Input port for the idempotent mark-read alert transition. */
public interface MarkAlertReadUseCase {

  /**
   * Marks an alert as read. Calling it again on a read alert is a no-op.
   *
   * @param id the alert id
   */
  void markRead(Long id);
}
