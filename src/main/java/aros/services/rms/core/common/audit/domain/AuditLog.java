package aros.services.rms.core.common.audit.domain;

import java.time.Instant;
import java.util.Optional;

public class AuditLog {
  private final String id;
  private final Long userId;
  private final String username;
  private final String businessAction;
  private final String targetEntity;
  private final String targetEntityId;
  private final String details;
  private final String oldValue;
  private final String newValue;
  private final String ipAddress;
  private final Instant timestamp;

  public AuditLog(
      String id,
      Long userId,
      String username,
      String businessAction,
      String targetEntity,
      String targetEntityId,
      String details,
      String oldValue,
      String newValue,
      String ipAddress,
      Instant timestamp) {
    this.id = id;
    this.userId = userId;
    this.username = username;
    this.businessAction = businessAction;
    this.targetEntity = targetEntity;
    this.targetEntityId = targetEntityId;
    this.details = details;
    this.oldValue = oldValue;
    this.newValue = newValue;
    this.ipAddress = ipAddress;
    this.timestamp = timestamp;
  }

  public String getId() {
    return id;
  }

  public Optional<Long> getUserId() {
    return Optional.ofNullable(userId);
  }

  public Optional<String> getUsername() {
    return Optional.ofNullable(username);
  }

  public String getBusinessAction() {
    return businessAction;
  }

  public String getTargetEntity() {
    return targetEntity;
  }

  public Optional<String> getTargetEntityId() {
    return Optional.ofNullable(targetEntityId);
  }

  public Optional<String> getDetails() {
    return Optional.ofNullable(details);
  }

  public Optional<String> getOldValue() {
    return Optional.ofNullable(oldValue);
  }

  public Optional<String> getNewValue() {
    return Optional.ofNullable(newValue);
  }

  public Optional<String> getIpAddress() {
    return Optional.ofNullable(ipAddress);
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String id;
    private Long userId;
    private String username;
    private String businessAction;
    private String targetEntity;
    private String targetEntityId;
    private String details;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private Instant timestamp;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    public Builder username(String username) {
      this.username = username;
      return this;
    }

    public Builder businessAction(String businessAction) {
      this.businessAction = businessAction;
      return this;
    }

    public Builder targetEntity(String targetEntity) {
      this.targetEntity = targetEntity;
      return this;
    }

    public Builder targetEntityId(String targetEntityId) {
      this.targetEntityId = targetEntityId;
      return this;
    }

    public Builder details(String details) {
      this.details = details;
      return this;
    }

    public Builder oldValue(String oldValue) {
      this.oldValue = oldValue;
      return this;
    }

    public Builder newValue(String newValue) {
      this.newValue = newValue;
      return this;
    }

    public Builder ipAddress(String ipAddress) {
      this.ipAddress = ipAddress;
      return this;
    }

    public Builder timestamp(Instant timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public AuditLog build() {
      return new AuditLog(
          id,
          userId,
          username,
          businessAction,
          targetEntity,
          targetEntityId,
          details,
          oldValue,
          newValue,
          ipAddress,
          timestamp);
    }
  }
}
