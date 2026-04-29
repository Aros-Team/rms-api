package aros.services.rms.infraestructure.audit.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for audit logs. */
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, String> {}
