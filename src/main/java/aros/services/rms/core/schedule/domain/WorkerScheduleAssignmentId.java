package aros.services.rms.core.schedule.domain;

public record WorkerScheduleAssignmentId(Long value) {
  public static WorkerScheduleAssignmentId of(Long value) {
    return new WorkerScheduleAssignmentId(value);
  }
}
