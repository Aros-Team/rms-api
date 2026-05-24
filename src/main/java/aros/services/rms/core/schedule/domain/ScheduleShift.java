package aros.services.rms.core.schedule.domain;

import java.time.LocalTime;

public record ScheduleShift(Long id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {

  public ScheduleShift {
    if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
      throw new IllegalArgumentException("startTime must be before endTime");
    }
  }

  public ScheduleShift(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
    this(null, dayOfWeek, startTime, endTime);
  }

  public boolean overlapsWith(ScheduleShift other) {
    if (this.dayOfWeek != other.dayOfWeek) {
      return false;
    }
    return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
  }
}
