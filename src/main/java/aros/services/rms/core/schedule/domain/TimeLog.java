package aros.services.rms.core.schedule.domain;

import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;

/** Domain entity representing a worker time log entry. */
public class TimeLog {
  private TimeLogId id;
  private UserId workerId;
  private Instant timestamp;
  private LogType type;
  private boolean withinShift;
  private Long relatedShiftId;

  /**
   * Creates a time log entry.
   *
   * @param id time log identifier, may be null for new entries
   * @param workerId identifier of the worker
   * @param timestamp when the log was recorded
   * @param type type of log (IN/OUT)
   * @param withinShift whether the log falls within a scheduled shift
   * @param relatedShiftId identifier of the related shift, if any
   */
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

  /** Creates a new clock-in time log entry. */
  public TimeLog(UserId workerId, Instant timestamp, boolean withinShift, Long relatedShiftId) {
    this(null, workerId, timestamp, LogType.IN, withinShift, relatedShiftId);
  }

  /** Returns the time log identifier. */
  public TimeLogId getId() {
    return id;
  }

  /** Returns the worker identifier. */
  public UserId getWorkerId() {
    return workerId;
  }

  /** Returns the timestamp of the log entry. */
  public Instant getTimestamp() {
    return timestamp;
  }

  /** Returns the log type (IN/OUT). */
  public LogType getType() {
    return type;
  }

  /** Returns whether this log falls within a scheduled shift. */
  public boolean isWithinShift() {
    return withinShift;
  }

  /** Returns the identifier of the related shift, or null. */
  public Long getRelatedShiftId() {
    return relatedShiftId;
  }

  /** Sets the time log identifier. */
  public void setId(TimeLogId id) {
    this.id = id;
  }
}
