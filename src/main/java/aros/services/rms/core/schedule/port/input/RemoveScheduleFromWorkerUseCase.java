package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.WorkerScheduleAssignmentId;

/** Use case for removing a schedule assignment from a worker. */
public interface RemoveScheduleFromWorkerUseCase {
  /**
   * Removes the schedule assignment identified by the given id.
   *
   * @param assignmentId the identifier of the assignment to remove
   */
  void remove(WorkerScheduleAssignmentId assignmentId);
}
