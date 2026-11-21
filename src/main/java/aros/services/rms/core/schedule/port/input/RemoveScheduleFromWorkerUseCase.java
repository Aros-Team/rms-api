package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.user.domain.UserId;

/** Use case for removing a schedule assignment from a worker. */
public interface RemoveScheduleFromWorkerUseCase {
  /**
   * Removes the assignment of the given schedule from the given worker.
   *
   * @param workerId the worker whose assignment is removed
   * @param scheduleId the schedule to unassign
   */
  void remove(UserId workerId, ScheduleId scheduleId);
}
