package aros.services.rms.core.common.audit.port.output;

import aros.services.rms.core.common.audit.domain.AuditLog;

public interface AuditLogPort {
  void save(AuditLog auditLog);
}
