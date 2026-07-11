package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.user.domain.UserId;

/** Use case for recording a time log entry for a worker. */
public interface RecordTimeLogUseCase {
  /**
   * Records a time log (clock-in/clock-out) for the given worker.
   *
   * @param workerId the identifier of the worker
   * @return the result indicating whether the log is within a scheduled shift
   */
  RecordTimeLogResult execute(UserId workerId);

  /**
   * Result of a time log recording operation.
   *
   * @param withinShift whether the log falls within a scheduled shift
   * @param activeShift the active shift at the time of logging, if any
   */
  record RecordTimeLogResult(boolean withinShift, ScheduleShift activeShift) {}
}
