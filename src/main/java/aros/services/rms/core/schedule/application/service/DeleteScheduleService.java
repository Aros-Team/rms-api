package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.exception.ScheduleHasAssignmentsException;
import aros.services.rms.core.schedule.application.exception.ScheduleNotFoundException;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.port.input.DeleteScheduleUseCase;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;

/** Service for deleting a schedule by its ID. */
public class DeleteScheduleService implements DeleteScheduleUseCase {
  private final ScheduleRepositoryPort scheduleRepository;
  private final WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  private final Logger logger;

  /**
   * Constructs a new DeleteScheduleService.
   *
   * @param scheduleRepository the schedule repository
   * @param assignmentRepository the assignment repository
   * @param logger the logger
   */
  public DeleteScheduleService(
      ScheduleRepositoryPort scheduleRepository,
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      Logger logger) {
    this.scheduleRepository = scheduleRepository;
    this.assignmentRepository = assignmentRepository;
    this.logger = logger;
  }

  /**
   * Deletes the schedule with the given ID if it exists and has no active assignments.
   *
   * @param id the schedule ID
   * @throws ScheduleNotFoundException if no schedule with the given ID exists
   * @throws ScheduleHasAssignmentsException if the schedule has active worker assignments
   */
  @Override
  public void delete(ScheduleId id) {
    if (scheduleRepository.findById(id).isEmpty()) {
      throw new ScheduleNotFoundException("Schedule not found with id: " + id.value());
    }

    if (assignmentRepository.existsByScheduleId(id)) {
      throw new ScheduleHasAssignmentsException(
          "Cannot delete schedule with id: " + id.value() + " because it has active assignments");
    }

    scheduleRepository.delete(id);
    logger.info("Schedule deleted: id={}", id.value());
  }
}
