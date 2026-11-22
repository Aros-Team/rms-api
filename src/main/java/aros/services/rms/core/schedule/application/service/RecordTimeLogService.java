/* (C) 2026 */

package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.domain.TimeLogSessions;
import aros.services.rms.core.schedule.port.input.RecordTimeLogUseCase;
import aros.services.rms.core.schedule.port.output.TimeLogRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Records the implicit clock-in performed at login.
 *
 * <p>Login is treated as a clock-in only when the worker authenticates inside an assigned shift. An
 * off-shift login is not persisted as attendance: doing so used to create an orphan entry that
 * blocked the explicit clock-in and paired with the wrong clock-out. An on-shift login is still
 * skipped when a session is already open, so repeated logins never double-count.
 */
public class RecordTimeLogService implements RecordTimeLogUseCase {
  private final TimeLogRepositoryPort timeLogRepository;
  private final WorkerShiftResolver shiftResolver;
  private final Clock clock;

  /**
   * Constructs a new RecordTimeLogService.
   *
   * @param timeLogRepository the time log repository
   * @param shiftResolver the resolver for the worker's assigned shifts
   * @param clock the clock used to stamp entries
   */
  public RecordTimeLogService(
      TimeLogRepositoryPort timeLogRepository, WorkerShiftResolver shiftResolver, Clock clock) {
    this.timeLogRepository = timeLogRepository;
    this.shiftResolver = shiftResolver;
    this.clock = clock;
  }

  /**
   * Records an implicit clock-in when the worker is inside a shift. The result still reports
   * whether the login happened within a shift, which drives the restricted access token.
   *
   * @param workerId the worker's user ID
   * @return the result indicating success and the matched shift, if any
   */
  @Override
  public RecordTimeLogResult execute(UserId workerId) {
    Instant now = clock.instant();
    Optional<ScheduleShift> activeShift = shiftResolver.activeShift(workerId, now);

    if (activeShift.isEmpty()) {
      return new RecordTimeLogResult(false, null);
    }

    boolean open =
        TimeLogSessions.isOpen(timeLogRepository.findLatestByWorkerId(workerId).orElse(null), now);
    if (!open) {
      timeLogRepository.save(new TimeLog(workerId, now, true, activeShift.get().id()));
    }
    return new RecordTimeLogResult(true, activeShift.get());
  }
}
