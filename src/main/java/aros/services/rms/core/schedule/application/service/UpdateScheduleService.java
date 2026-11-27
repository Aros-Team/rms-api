package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.exception.ScheduleAlreadyExistsException;
import aros.services.rms.core.schedule.application.exception.ScheduleNotFoundException;
import aros.services.rms.core.schedule.application.exception.ShiftOverlapException;
import aros.services.rms.core.schedule.application.exception.WeeklyHoursExceededException;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.schedule.port.input.UpdateScheduleUseCase;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Service for updating an existing schedule's name, description, and shifts. */
public class UpdateScheduleService implements UpdateScheduleUseCase {
  /** Hard weekly cap shared with the assignment flow. */
  public static final int HARD_WEEKLY_CAP_HOURS = 42;

  private final ScheduleRepositoryPort scheduleRepository;
  private final WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  private final Logger logger;

  /**
   * Constructs a new UpdateScheduleService.
   *
   * @param scheduleRepository the schedule repository
   * @param assignmentRepository the worker-assignment repository
   * @param logger the logger
   */
  public UpdateScheduleService(
      ScheduleRepositoryPort scheduleRepository,
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      Logger logger) {
    this.scheduleRepository = scheduleRepository;
    this.assignmentRepository = assignmentRepository;
    this.logger = logger;
  }

  /**
   * Updates the schedule with the given ID using the provided info.
   *
   * @param id the schedule ID to update
   * @param info the update info containing new name, description, and shifts
   * @return the updated schedule
   * @throws ScheduleNotFoundException if no schedule with the given ID exists
   * @throws ScheduleAlreadyExistsException if another schedule with the new name already exists
   */
  @Override
  public Schedule update(ScheduleId id, UpdateScheduleInfo info) {
    Schedule existing =
        scheduleRepository
            .findById(id)
            .orElseThrow(
                () -> new ScheduleNotFoundException("Horario no encontrado con id: " + id.value()));

    if (!existing.getName().equals(info.name()) && scheduleRepository.existsByName(info.name())) {
      throw new ScheduleAlreadyExistsException(
          "Ya existe un horario con el nombre: " + info.name());
    }

    List<ScheduleShift> shifts =
        info.shifts().stream()
            .map(
                s ->
                    new ScheduleShift(
                        DayOfWeek.valueOf(s.dayOfWeek()),
                        LocalTime.parse(s.startTime()),
                        LocalTime.parse(s.endTime())))
            .toList();

    existing.setName(info.name());
    existing.setDescription(info.description());
    existing.setShifts(shifts);

    // Updating a template changes it for every assigned worker, so the same
    // rules enforced on assignment must be re-checked before persisting.
    validateAssignedWorkers(existing);

    Schedule saved = scheduleRepository.save(existing);
    logger.info("Schedule updated: id={}, name={}", saved.getId().value(), saved.getName());
    return saved;
  }

  private void validateAssignedWorkers(Schedule schedule) {
    List<WorkerScheduleAssignment> assignments =
        assignmentRepository.findByScheduleId(schedule.getId());

    for (WorkerScheduleAssignment assignment : assignments) {
      List<Schedule> otherSchedules =
          assignmentRepository.findByWorkerId(assignment.getWorkerId()).stream()
              .filter(other -> !other.getScheduleId().equals(schedule.getId()))
              .map(other -> scheduleRepository.findById(other.getScheduleId()).orElse(null))
              .filter(Objects::nonNull)
              .toList();

      for (ScheduleShift updatedShift : schedule.getShifts()) {
        for (Schedule other : otherSchedules) {
          for (ScheduleShift existingShift : other.getShifts()) {
            if (updatedShift.overlapsWith(existingShift)) {
              throw new ShiftOverlapException(
                  "Shift overlap detected",
                  "El turno "
                      + updatedShift
                      + " se superpone con el turno existente "
                      + existingShift
                      + " del horario "
                      + other.getName());
            }
          }
        }
      }

      long totalMinutes =
          weeklyMinutes(schedule) + otherSchedules.stream().mapToLong(this::weeklyMinutes).sum();
      double weeklyHours = totalMinutes / 60.0;
      if (totalMinutes > HARD_WEEKLY_CAP_HOURS * 60L) {
        throw new WeeklyHoursExceededException(
            String.format(
                Locale.ROOT,
                "El trabajador %d quedaría con %.2f h/semana, por encima del tope de %d h/semana",
                assignment.getWorkerId().value(),
                weeklyHours,
                HARD_WEEKLY_CAP_HOURS),
            weeklyHours,
            HARD_WEEKLY_CAP_HOURS);
      }
    }
  }

  private long weeklyMinutes(Schedule schedule) {
    return schedule.getShifts().stream()
        .mapToLong(shift -> Duration.between(shift.startTime(), shift.endTime()).toMinutes())
        .sum();
  }
}
