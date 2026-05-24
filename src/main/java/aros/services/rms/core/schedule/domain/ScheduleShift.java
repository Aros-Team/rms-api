package aros.services.rms.core.schedule.domain;

import java.time.LocalTime;

/** Record representing a shift within a schedule. */
public record ScheduleShift(Long id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {

  /** Compact constructor validating that startTime is before endTime. */
  public ScheduleShift {
    if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
      throw new IllegalArgumentException("startTime must be before endTime");
    }
  }

  /** Creates a new shift without an id (id will be assigned by persistence). */
  public ScheduleShift(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
    this(null, dayOfWeek, startTime, endTime);
  }

  /** Returns true if this shift overlaps with the given shift on the same day. */
  public boolean overlapsWith(ScheduleShift other) {
    if (this.dayOfWeek != other.dayOfWeek) {
      return false;
    }
    return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
  }
}
