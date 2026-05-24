package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.exception.ScheduleNotFoundException;
import aros.services.rms.core.schedule.application.exception.ShiftOverlapException;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.schedule.port.input.AssignScheduleToWorkerUseCase;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import java.util.List;

public class AssignScheduleToWorkerService implements AssignScheduleToWorkerUseCase {
  private final ScheduleRepositoryPort scheduleRepository;
  private final WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  private final Logger logger;

  public AssignScheduleToWorkerService(
      ScheduleRepositoryPort scheduleRepository,
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      Logger logger) {
    this.scheduleRepository = scheduleRepository;
    this.assignmentRepository = assignmentRepository;
    this.logger = logger;
  }

  @Override
  public WorkerScheduleAssignment assign(AssignInfo info) {
    Schedule schedule =
        scheduleRepository
            .findById(new ScheduleId(info.scheduleId()))
            .orElseThrow(
                () ->
                    new ScheduleNotFoundException(
                        "Schedule not found with id: " + info.scheduleId()));

    List<WorkerScheduleAssignment> existingAssignments =
        assignmentRepository.findByWorkerId(info.workerId());

    List<ScheduleShift> allExistingShifts =
        existingAssignments.stream()
            .flatMap(ass -> scheduleRepository.findById(ass.getScheduleId()).stream())
            .flatMap(s -> s.getShifts().stream())
            .toList();

    for (ScheduleShift newShift : schedule.getShifts()) {
      for (ScheduleShift existingShift : allExistingShifts) {
        if (newShift.overlapsWith(existingShift)) {
          throw new ShiftOverlapException(
              "Shift overlap detected",
              "New shift " + newShift + " overlaps with existing shift " + existingShift);
        }
      }
    }

    WorkerScheduleAssignment assignment =
        new WorkerScheduleAssignment(info.workerId(), schedule.getId());
    WorkerScheduleAssignment saved = assignmentRepository.save(assignment);
    logger.info(
        "Schedule assigned: workerId={}, scheduleId={}",
        info.workerId().value(),
        info.scheduleId());
    return saved;
  }
}
