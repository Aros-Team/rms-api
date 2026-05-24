package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;
import java.util.List;

/** Use case for retrieving time log history of a worker. */
public interface GetTimeLogHistoryUseCase {
  /**
   * Returns time logs matching the given filter.
   *
   * @param filter the filtering criteria
   * @return list of matching time logs
   */
  List<TimeLog> getHistory(TimeLogFilter filter);

  /**
   * Filter criteria for querying time log history.
   *
   * @param workerId the worker to filter by
   * @param from the start of the time range
   * @param to the end of the time range
   * @param withinShift whether to include only logs within scheduled shifts
   */
  record TimeLogFilter(UserId workerId, Instant from, Instant to, Boolean withinShift) {}
}
