/* (C) 2026 */

package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.exception.NoOpenSessionException;
import aros.services.rms.core.schedule.application.exception.OpenSessionAlreadyExistsException;
import aros.services.rms.core.schedule.domain.LogType;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.domain.TimeLogSessions;
import aros.services.rms.core.schedule.port.input.ClockInUseCase;
import aros.services.rms.core.schedule.port.input.ClockOutUseCase;
import aros.services.rms.core.schedule.port.output.TimeLogRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Records explicit worker clock-in and clock-out entries.
 *
 * <p>Only one session may be open per worker. Login also creates an implicit clock-in when the
 * worker authenticates inside a shift; this service refuses to open a second session in that case.
 * The active shift is resolved at write time so the entry carries an accurate {@code withinShift}
 * flag and shift reference.
 */
public class ClockAttendanceService implements ClockInUseCase, ClockOutUseCase {
  private final TimeLogRepositoryPort timeLogRepository;
  private final WorkerShiftResolver shiftResolver;
  private final Logger logger;
  private final Clock clock;

  /**
   * Constructs a new ClockAttendanceService.
   *
   * @param timeLogRepository the time log repository
   * @param shiftResolver the resolver for the worker's assigned shifts
   * @param logger the logger
   * @param clock the clock used to stamp entries
   */
  public ClockAttendanceService(
      TimeLogRepositoryPort timeLogRepository,
      WorkerShiftResolver shiftResolver,
      Logger logger,
      Clock clock) {
    this.timeLogRepository = timeLogRepository;
    this.shiftResolver = shiftResolver;
    this.logger = logger;
    this.clock = clock;
  }

  /**
   * Opens a clock-in session for the worker.
   *
   * @param workerId the worker who clocks in
   * @return the created clock-in entry
   * @throws OpenSessionAlreadyExistsException if a recent clock-in has no matching clock-out
   */
  @Override
  public TimeLog clockIn(UserId workerId) {
    Instant now = clock.instant();
    Optional<TimeLog> latest = timeLogRepository.findLatestByWorkerId(workerId);

    if (TimeLogSessions.isOpen(latest.orElse(null), now)) {
      throw new OpenSessionAlreadyExistsException(
          "Worker "
              + workerId.value()
              + " already has an open clock-in session since "
              + latest.get().getTimestamp());
    }

    Optional<ScheduleShift> activeShift = shiftResolver.activeShift(workerId, now);
    TimeLog log =
        new TimeLog(
            null,
            workerId,
            now,
            LogType.IN,
            activeShift.isPresent(),
            activeShift.map(ScheduleShift::id).orElse(null));
    TimeLog saved = timeLogRepository.save(log);
    logger.info(
        "Clock-in recorded: workerId={}, withinShift={}, shiftId={}",
        workerId.value(),
        saved.isWithinShift(),
        saved.getRelatedShiftId());
    return saved;
  }

  /**
   * Closes the worker's open clock-in session.
   *
   * @param workerId the worker who clocks out
   * @return the created clock-out entry
   * @throws NoOpenSessionException if the latest entry is not a recent clock-in
   */
  @Override
  public TimeLog clockOut(UserId workerId) {
    Instant now = clock.instant();
    Optional<TimeLog> latest = timeLogRepository.findLatestByWorkerId(workerId);

    if (!TimeLogSessions.isOpen(latest.orElse(null), now)) {
      throw new NoOpenSessionException(
          "Worker " + workerId.value() + " has no open clock-in session to close");
    }

    Optional<ScheduleShift> activeShift = shiftResolver.activeShift(workerId, now);
    TimeLog log =
        new TimeLog(
            null,
            workerId,
            now,
            LogType.OUT,
            activeShift.isPresent(),
            activeShift.map(ScheduleShift::id).orElse(null));
    TimeLog saved = timeLogRepository.save(log);
    logger.info(
        "Clock-out recorded: workerId={}, withinShift={}, shiftId={}",
        workerId.value(),
        saved.isWithinShift(),
        saved.getRelatedShiftId());
    return saved;
  }
}
