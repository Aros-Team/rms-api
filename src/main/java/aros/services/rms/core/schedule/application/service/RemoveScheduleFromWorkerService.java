package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignmentId;
import aros.services.rms.core.schedule.port.input.RemoveScheduleFromWorkerUseCase;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;

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
   * Removes the schedule assignment identified by the given ID.
   *
   * @param assignmentId the assignment ID to remove
   */
  @Override
  public void remove(WorkerScheduleAssignmentId assignmentId) {
    assignmentRepository.delete(assignmentId);
    logger.info("Schedule assignment removed: id={}", assignmentId.value());
  }
}
