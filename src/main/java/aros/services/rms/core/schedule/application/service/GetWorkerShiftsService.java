package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.port.input.GetWorkerShiftsUseCase;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetWorkerShiftsService implements GetWorkerShiftsUseCase {
  private final WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  private final ScheduleRepositoryPort scheduleRepository;

  public GetWorkerShiftsService(
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      ScheduleRepositoryPort scheduleRepository) {
    this.assignmentRepository = assignmentRepository;
    this.scheduleRepository = scheduleRepository;
  }

  @Override
  public Map<DayOfWeek, List<ShiftDetail>> getShifts(UserId workerId) {
    Map<DayOfWeek, List<ShiftDetail>> result = new LinkedHashMap<>();
    for (DayOfWeek day : DayOfWeek.values()) {
      result.put(day, new ArrayList<>());
    }

    var assignments = assignmentRepository.findByWorkerId(workerId);

    for (var assignment : assignments) {
      scheduleRepository
          .findById(assignment.getScheduleId())
          .ifPresent(
              schedule -> {
                for (ScheduleShift shift : schedule.getShifts()) {
                  result
                      .get(shift.dayOfWeek())
                      .add(new ShiftDetail(schedule.getName(), shift.startTime(), shift.endTime()));
                }
              });
    }

    return result;
  }
}
