package aros.services.rms.core.schedule.port.output;

import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Repository port for TimeLog persistence operations. */
public interface TimeLogRepositoryPort {
  /** Persists a time log entry. */
  TimeLog save(TimeLog timeLog);

  /** Finds the most recent time log entry for a worker, if any. */
  Optional<TimeLog> findLatestByWorkerId(UserId workerId);

  /** Finds all time logs for a worker within a date range. */
  List<TimeLog> findByWorkerIdAndDateRange(UserId workerId, Instant from, Instant to);

  /** Finds all time logs within a date range. */
  List<TimeLog> findAllByDateRange(Instant from, Instant to);
}
