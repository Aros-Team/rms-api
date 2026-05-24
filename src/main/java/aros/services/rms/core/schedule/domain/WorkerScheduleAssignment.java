package aros.services.rms.core.schedule.domain;

import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;

/** Domain entity representing the assignment of a worker to a schedule. */
public class WorkerScheduleAssignment {
  private WorkerScheduleAssignmentId id;
  private UserId workerId;
  private ScheduleId scheduleId;
  private Instant assignedAt;

  /**
   * Creates a worker-schedule assignment.
   *
   * @param id assignment identifier, may be null for new assignments
   * @param workerId identifier of the assigned worker
   * @param scheduleId identifier of the assigned schedule
   * @param assignedAt timestamp when the assignment was made
   */
  public WorkerScheduleAssignment(
      WorkerScheduleAssignmentId id, UserId workerId, ScheduleId scheduleId, Instant assignedAt) {
    this.id = id;
    this.workerId = workerId;
    this.scheduleId = scheduleId;
    this.assignedAt = assignedAt;
  }

  /** Creates a new assignment with the current timestamp. */
  public WorkerScheduleAssignment(UserId workerId, ScheduleId scheduleId) {
    this(null, workerId, scheduleId, Instant.now());
  }

  /** Returns the assignment identifier. */
  public WorkerScheduleAssignmentId getId() {
    return id;
  }

  /** Returns the assigned worker identifier. */
  public UserId getWorkerId() {
    return workerId;
  }

  /** Returns the assigned schedule identifier. */
  public ScheduleId getScheduleId() {
    return scheduleId;
  }

  /** Returns the timestamp when the assignment was created. */
  public Instant getAssignedAt() {
    return assignedAt;
  }

  /** Sets the assignment identifier. */
  public void setId(WorkerScheduleAssignmentId id) {
    this.id = id;
  }
}
