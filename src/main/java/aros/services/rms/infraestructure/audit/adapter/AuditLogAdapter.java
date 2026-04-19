package aros.services.rms.infraestructure.audit.adapter;

import aros.services.rms.core.common.audit.domain.AuditLog;
import aros.services.rms.core.common.audit.port.output.AuditLogPort;
import aros.services.rms.infraestructure.audit.persistence.AuditLogEntity;
import aros.services.rms.infraestructure.audit.persistence.AuditLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Transactional
public class AuditLogAdapter implements AuditLogPort {

  @Autowired private AuditLogRepository repository;

  @Override
  @Async("virtualThreadExecutor")
  public void save(AuditLog auditLog) {
    AuditLogEntity entity = toEntity(auditLog);
    repository.save(entity);
  }

  private AuditLogEntity toEntity(AuditLog auditLog) {
    AuditLogEntity entity = new AuditLogEntity();
    entity.setId(auditLog.getId());
    auditLog
        .getUserId()
        .ifPresent(
            id -> {
              entity.setUserId(id);
            });
    auditLog
        .getUsername()
        .ifPresent(
            username -> {
              entity.setUsername(username);
            });
    entity.setBusinessAction(auditLog.getBusinessAction());
    entity.setTargetEntity(auditLog.getTargetEntity());
    auditLog
        .getTargetEntityId()
        .ifPresent(
            targetEntityId -> {
              entity.setTargetEntityId(targetEntityId);
            });
    auditLog
        .getDetails()
        .ifPresent(
            details -> {
              entity.setDetails(details);
            });
    auditLog
        .getOldValue()
        .ifPresent(
            oldValue -> {
              entity.setOldValue(oldValue);
            });
    auditLog
        .getNewValue()
        .ifPresent(
            newValue -> {
              entity.setNewValue(newValue);
            });
    auditLog
        .getIpAddress()
        .ifPresent(
            ip -> {
              entity.setIpAddress(ip);
            });
    entity.setTimestamp(auditLog.getTimestamp());
    return entity;
  }
}
