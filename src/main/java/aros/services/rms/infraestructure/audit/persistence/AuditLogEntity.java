package aros.services.rms.infraestructure.audit.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

  @Id
  @Column(length = 36)
  private String id;

  @Column(name = "user_id")
  private Long userId;

  private String username;

  @Column(name = "business_action", length = 100, nullable = false)
  private String businessAction;

  @Column(name = "target_entity", length = 100, nullable = false)
  private String targetEntity;

  @Column(name = "target_entity_id", length = 255)
  private String targetEntityId;

  @Column(columnDefinition = "JSON")
  private String details;

  @Column(name = "old_value", columnDefinition = "JSON")
  private String oldValue;

  @Column(name = "new_value", columnDefinition = "JSON")
  private String newValue;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(nullable = false)
  private Instant timestamp;

  public AuditLogEntity() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getBusinessAction() {
    return businessAction;
  }

  public void setBusinessAction(String businessAction) {
    this.businessAction = businessAction;
  }

  public String getTargetEntity() {
    return targetEntity;
  }

  public void setTargetEntity(String targetEntity) {
    this.targetEntity = targetEntity;
  }

  public String getTargetEntityId() {
    return targetEntityId;
  }

  public void setTargetEntityId(String targetEntityId) {
    this.targetEntityId = targetEntityId;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public String getOldValue() {
    return oldValue;
  }

  public void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }

  public String getNewValue() {
    return newValue;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }
}
