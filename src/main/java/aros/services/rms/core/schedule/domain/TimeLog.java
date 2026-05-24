package aros.services.rms.core.schedule.domain;

import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;

public class TimeLog {
  private TimeLogId id;
  private UserId workerId;
  private Instant timestamp;
  private LogType type;
  private boolean withinShift;
  private Long relatedShiftId;

  public TimeLog(
      TimeLogId id,
      UserId workerId,
      Instant timestamp,
      LogType type,
      boolean withinShift,
      Long relatedShiftId) {
    this.id = id;
    this.workerId = workerId;
    this.timestamp = timestamp;
    this.type = type;
    this.withinShift = withinShift;
    this.relatedShiftId = relatedShiftId;
  }

  public TimeLog(UserId workerId, Instant timestamp, boolean withinShift, Long relatedShiftId) {
    this(null, workerId, timestamp, LogType.IN, withinShift, relatedShiftId);
  }

  public TimeLogId getId() {
    return id;
  }

  public UserId getWorkerId() {
    return workerId;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public LogType getType() {
    return type;
  }

  public boolean isWithinShift() {
    return withinShift;
  }

  public Long getRelatedShiftId() {
    return relatedShiftId;
  }

  public void setId(TimeLogId id) {
    this.id = id;
  }
}
