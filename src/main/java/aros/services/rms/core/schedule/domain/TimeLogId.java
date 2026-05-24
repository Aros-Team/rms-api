package aros.services.rms.core.schedule.domain;

/** Value object for time log identifier. */
public record TimeLogId(Long value) {
  /** Creates a TimeLogId from a Long value. */
  public static TimeLogId of(Long value) {
    return new TimeLogId(value);
  }
}
