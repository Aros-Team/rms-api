/* (C) 2026 */

package aros.services.rms.core.common.audit.domain;

import java.time.Instant;
import java.util.Optional;

/** Domain model representing an audit log entry for tracking business changes. */
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

  /**
   * Creates an audit log entry.
   *
   * @param id unique identifier
   * @param userId user ID
   * @param username username
   * @param businessAction action performed
   * @param targetEntity target entity type
   * @param targetEntityId target entity ID
   * @param details additional details
   * @param oldValue previous value
   * @param newValue new value
   * @param ipAddress IP address
   * @param timestamp timestamp
   */
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

  /**
   * Creates a new builder for AuditLog.
   *
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for constructing AuditLog instances. */
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

    /** Sets the audit log ID. */
    public Builder id(String id) {
      this.id = id;
      return this;
    }

    /** Sets the user ID. */
    public Builder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    /** Sets the username. */
    public Builder username(String username) {
      this.username = username;
      return this;
    }

    /** Sets the business action. */
    public Builder businessAction(String businessAction) {
      this.businessAction = businessAction;
      return this;
    }

    /** Sets the target entity. */
    public Builder targetEntity(String targetEntity) {
      this.targetEntity = targetEntity;
      return this;
    }

    /** Sets the target entity ID. */
    public Builder targetEntityId(String targetEntityId) {
      this.targetEntityId = targetEntityId;
      return this;
    }

    /** Sets the details. */
    public Builder details(String details) {
      this.details = details;
      return this;
    }

    /** Sets the old value. */
    public Builder oldValue(String oldValue) {
      this.oldValue = oldValue;
      return this;
    }

    /** Sets the new value. */
    public Builder newValue(String newValue) {
      this.newValue = newValue;
      return this;
    }

    /** Sets the IP address. */
    public Builder ipAddress(String ipAddress) {
      this.ipAddress = ipAddress;
      return this;
    }

    /** Sets the timestamp. */
    public Builder timestamp(Instant timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    /**
     * Builds the AuditLog instance.
     *
     * @return the built AuditLog
     */
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
