package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.user.domain.UserId;

/** Use case for assigning a schedule to a worker. */
public interface AssignScheduleToWorkerUseCase {
  /**
   * Assigns a schedule to a worker and returns the assignment.
   *
   * @param info the worker and schedule to assign
   * @return the created assignment
   */
  WorkerScheduleAssignment assign(AssignInfo info);

  /**
   * Information required to assign a schedule to a worker.
   *
   * @param workerId the identifier of the worker
   * @param scheduleId the identifier of the schedule
   */
  record AssignInfo(UserId workerId, Long scheduleId) {}
}
