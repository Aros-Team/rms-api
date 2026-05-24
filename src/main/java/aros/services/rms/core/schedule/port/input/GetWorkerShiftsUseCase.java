package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.user.domain.UserId;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/** Use case for retrieving the weekly shifts of a worker. */
public interface GetWorkerShiftsUseCase {
  /**
   * Returns the shifts grouped by day of week for the given worker.
   *
   * @param workerId the identifier of the worker
   * @return map of day of week to list of shift details
   */
  Map<DayOfWeek, List<ShiftDetail>> getShifts(UserId workerId);

  /**
   * Details of a single shift for display purposes.
   *
   * @param scheduleName the name of the associated schedule
   * @param startTime the shift start time
   * @param endTime the shift end time
   */
  record ShiftDetail(String scheduleName, LocalTime startTime, LocalTime endTime) {}
}
