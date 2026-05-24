package aros.services.rms.core.schedule.domain;

public record TimeLogId(Long value) {
  public static TimeLogId of(Long value) {
    return new TimeLogId(value);
  }
}
