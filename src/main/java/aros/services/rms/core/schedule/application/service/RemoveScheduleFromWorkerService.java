package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.port.input.RemoveScheduleFromWorkerUseCase;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import aros.services.rms.core.user.domain.UserId;

/** Service for removing a schedule assignment from a worker. */
public class RemoveScheduleFromWorkerService implements RemoveScheduleFromWorkerUseCase {
  private final WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  private final Logger logger;

  /**
   * Constructs a new RemoveScheduleFromWorkerService.
   *
   * @param assignmentRepository the assignment repository
   * @param logger the logger
   */
  public RemoveScheduleFromWorkerService(
      WorkerScheduleAssignmentRepositoryPort assignmentRepository, Logger logger) {
    this.assignmentRepository = assignmentRepository;
    this.logger = logger;
  }

  /**
   * Removes the assignment of the given schedule from the given worker.
   *
   * @param workerId the worker whose assignment is removed
   * @param scheduleId the schedule to unassign
   */
  @Override
  public void remove(UserId workerId, ScheduleId scheduleId) {
    assignmentRepository.deleteByWorkerIdAndScheduleId(workerId, scheduleId);
    logger.info(
        "Schedule assignment removed: workerId={}, scheduleId={}",
        workerId.value(),
        scheduleId.value());
  }
}
