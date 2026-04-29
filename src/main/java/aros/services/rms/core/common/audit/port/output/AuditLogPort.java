/* (C) 2026 */

package aros.services.rms.core.common.audit.port.output;

import aros.services.rms.core.common.audit.domain.AuditLog;

/** Output port for audit log persistence operations. */
public interface AuditLogPort {
  /**
   * Saves an audit log entry.
   *
   * @param auditLog the audit log to save
   */
  void save(AuditLog auditLog);
}
