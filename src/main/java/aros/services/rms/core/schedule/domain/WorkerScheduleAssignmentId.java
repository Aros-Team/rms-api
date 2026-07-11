package aros.services.rms.core.schedule.domain;

/** Value object for worker schedule assignment identifier. */
public record WorkerScheduleAssignmentId(Long value) {
  /** Creates a WorkerScheduleAssignmentId from a Long value. */
  public static WorkerScheduleAssignmentId of(Long value) {
    return new WorkerScheduleAssignmentId(value);
  }
}
