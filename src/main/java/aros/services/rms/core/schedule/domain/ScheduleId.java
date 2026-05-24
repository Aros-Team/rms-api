package aros.services.rms.core.schedule.domain;

public record ScheduleId(Long value) {
  public static ScheduleId of(Long value) {
    return new ScheduleId(value);
  }
}
