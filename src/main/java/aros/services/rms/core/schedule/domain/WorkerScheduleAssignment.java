package aros.services.rms.core.schedule.domain;

import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;

public class WorkerScheduleAssignment {
  private WorkerScheduleAssignmentId id;
  private UserId workerId;
  private ScheduleId scheduleId;
  private Instant assignedAt;

  public WorkerScheduleAssignment(
      WorkerScheduleAssignmentId id, UserId workerId, ScheduleId scheduleId, Instant assignedAt) {
    this.id = id;
    this.workerId = workerId;
    this.scheduleId = scheduleId;
    this.assignedAt = assignedAt;
  }

  public WorkerScheduleAssignment(UserId workerId, ScheduleId scheduleId) {
    this(null, workerId, scheduleId, Instant.now());
  }

  public WorkerScheduleAssignmentId getId() {
    return id;
  }

  public UserId getWorkerId() {
    return workerId;
  }

  public ScheduleId getScheduleId() {
    return scheduleId;
  }

  public Instant getAssignedAt() {
    return assignedAt;
  }

  public void setId(WorkerScheduleAssignmentId id) {
    this.id = id;
  }
}
