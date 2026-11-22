/* (C) 2026 */

package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.port.input.GetTimeLogHistoryUseCase;
import aros.services.rms.core.schedule.port.output.TimeLogRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Retrieves time log history and applies the {@code withinShift} filter against the worker's
 * current assigned shifts.
 *
 * <p>The filter is evaluated from the schedule rather than trusting the stored flag, so old rows
 * and records written by other writers cannot drift from the worker's actual shift.
 */
public class GetTimeLogHistoryService implements GetTimeLogHistoryUseCase {
  private final TimeLogRepositoryPort timeLogRepository;
  private final WorkerShiftResolver shiftResolver;

  /**
   * Constructs a new GetTimeLogHistoryService.
   *
   * @param timeLogRepository the time log repository
   * @param shiftResolver the resolver for workers' assigned shifts
   */
  public GetTimeLogHistoryService(
      TimeLogRepositoryPort timeLogRepository, WorkerShiftResolver shiftResolver) {
    this.timeLogRepository = timeLogRepository;
    this.shiftResolver = shiftResolver;
  }

  /**
   * Retrieves time logs matching the given filter criteria.
   *
   * @param filter the filter with optional worker ID, date range and within-shift flag
   * @return a list of matching time logs
   */
  @Override
  public List<TimeLog> getHistory(TimeLogFilter filter) {
    List<TimeLog> logs;
    if (filter.workerId() != null) {
      logs =
          timeLogRepository.findByWorkerIdAndDateRange(
              filter.workerId(),
              filter.from() != null ? filter.from() : Instant.EPOCH,
              filter.to() != null ? filter.to() : Instant.now());
    } else {
      logs =
          timeLogRepository.findAllByDateRange(
              filter.from() != null ? filter.from() : Instant.EPOCH,
              filter.to() != null ? filter.to() : Instant.now());
    }

    if (filter.withinShift() == null) {
      return logs;
    }

    boolean expected = filter.withinShift();
    Map<UserId, List<ScheduleShift>> shiftsByWorker = new HashMap<>();
    return logs.stream()
        .filter(
            log -> {
              List<ScheduleShift> shifts =
                  shiftsByWorker.computeIfAbsent(log.getWorkerId(), shiftResolver::shiftsFor);
              return shiftResolver.isWithinShift(shifts, log.getTimestamp()) == expected;
            })
        .toList();
  }
}
