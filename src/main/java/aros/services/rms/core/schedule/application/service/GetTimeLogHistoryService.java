package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.port.input.GetTimeLogHistoryUseCase;
import aros.services.rms.core.schedule.port.output.TimeLogRepositoryPort;
import java.time.Instant;
import java.util.List;

public class GetTimeLogHistoryService implements GetTimeLogHistoryUseCase {
  private final TimeLogRepositoryPort timeLogRepository;

  public GetTimeLogHistoryService(TimeLogRepositoryPort timeLogRepository) {
    this.timeLogRepository = timeLogRepository;
  }

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
