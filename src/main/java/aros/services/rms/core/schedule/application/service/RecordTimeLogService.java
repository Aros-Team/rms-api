package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.port.input.RecordTimeLogUseCase;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.TimeLogRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/** Service for recording a time log entry when a worker clocks in during an active shift. */
public class RecordTimeLogService implements RecordTimeLogUseCase {
  private final TimeLogRepositoryPort timeLogRepository;
  private final WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  private final ScheduleRepositoryPort scheduleRepository;
  private final ZoneId zoneId;

  /**
   * Constructs a new RecordTimeLogService.
   *
   * @param timeLogRepository the time log repository
   * @param assignmentRepository the assignment repository
   * @param scheduleRepository the schedule repository
   * @param zoneId the time zone for determining the current day
   */
  public RecordTimeLogService(
      TimeLogRepositoryPort timeLogRepository,
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      ScheduleRepositoryPort scheduleRepository,
      ZoneId zoneId) {
    this.timeLogRepository = timeLogRepository;
    this.assignmentRepository = assignmentRepository;
    this.scheduleRepository = scheduleRepository;
    this.zoneId = zoneId;
  }

  /**
   * Records a time log for the worker. If the worker has an active shift at this moment, the log is
   * marked as successful; otherwise, it is recorded as a failed attempt.
   *
   * @param workerId the worker's user ID
   * @return the result indicating success and the matched shift, if any
   */
  @Override
  public RecordTimeLogResult execute(UserId workerId) {
    Instant now = Instant.now();
    LocalDate localDate = now.atZone(zoneId).toLocalDate();
    LocalTime localTime = now.atZone(zoneId).toLocalTime();
    DayOfWeek today = DayOfWeek.valueOf(localDate.getDayOfWeek().name());

    var assignments = assignmentRepository.findByWorkerId(workerId);

    for (var assignment : assignments) {
      Schedule schedule = scheduleRepository.findById(assignment.getScheduleId()).orElse(null);
      if (schedule == null) {
        continue;
      }

      for (ScheduleShift shift : schedule.getShifts()) {
        if (shift.dayOfWeek() == today
            && !localTime.isBefore(shift.startTime())
            && localTime.isBefore(shift.endTime())) {

          TimeLog log = new TimeLog(workerId, now, true, shift.id());
          timeLogRepository.save(log);
          return new RecordTimeLogResult(true, shift);
        }
      }
    }

    TimeLog log = new TimeLog(workerId, now, false, null);
    timeLogRepository.save(log);
    return new RecordTimeLogResult(false, null);
  }
}
