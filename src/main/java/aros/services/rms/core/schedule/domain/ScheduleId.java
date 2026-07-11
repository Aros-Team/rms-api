package aros.services.rms.core.schedule.domain;

/** Value object for schedule identifier. */
public record ScheduleId(Long value) {
  /** Creates a ScheduleId from a Long value. */
  public static ScheduleId of(Long value) {
    return new ScheduleId(value);
  }
}
