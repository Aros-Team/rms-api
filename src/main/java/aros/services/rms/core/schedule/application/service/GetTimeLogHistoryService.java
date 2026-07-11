package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.port.input.GetTimeLogHistoryUseCase;
import aros.services.rms.core.schedule.port.output.TimeLogRepositoryPort;
import java.time.Instant;
import java.util.List;

/** Service for retrieving time log history with optional worker and date-range filtering. */
public class GetTimeLogHistoryService implements GetTimeLogHistoryUseCase {
  private final TimeLogRepositoryPort timeLogRepository;

  /**
   * Constructs a new GetTimeLogHistoryService.
   *
   * @param timeLogRepository the time log repository
   */
  public GetTimeLogHistoryService(TimeLogRepositoryPort timeLogRepository) {
    this.timeLogRepository = timeLogRepository;
  }

  /**
   * Retrieves time logs matching the given filter criteria.
   *
   * @param filter the filter with optional worker ID and date range
   * @return a list of matching time logs
   */
  @Override
  public List<TimeLog> getHistory(TimeLogFilter filter) {
    if (filter.workerId() != null) {
      return timeLogRepository.findByWorkerIdAndDateRange(
          filter.workerId(),
          filter.from() != null ? filter.from() : Instant.EPOCH,
          filter.to() != null ? filter.to() : Instant.now());
    }
    return timeLogRepository.findAllByDateRange(
        filter.from() != null ? filter.from() : Instant.EPOCH,
        filter.to() != null ? filter.to() : Instant.now());
  }
}
