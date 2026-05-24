package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.exception.ScheduleHasAssignmentsException;
import aros.services.rms.core.schedule.application.exception.ScheduleNotFoundException;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.port.input.DeleteScheduleUseCase;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;

public class DeleteScheduleService implements DeleteScheduleUseCase {
  private final ScheduleRepositoryPort scheduleRepository;
  private final WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  private final Logger logger;

  public DeleteScheduleService(
      ScheduleRepositoryPort scheduleRepository,
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      Logger logger) {
    this.scheduleRepository = scheduleRepository;
    this.assignmentRepository = assignmentRepository;
    this.logger = logger;
  }

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
