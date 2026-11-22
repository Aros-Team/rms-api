/* (C) 2026 */

package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.exception.ScheduleAlreadyAssignedException;
import aros.services.rms.core.schedule.application.exception.ScheduleNotFoundException;
import aros.services.rms.core.schedule.application.exception.ShiftOverlapException;
import aros.services.rms.core.schedule.application.exception.WeeklyHoursExceededException;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.schedule.port.input.AssignScheduleToWorkerUseCase;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Service for assigning a schedule to a worker.
 *
 * <p>Before persisting, the assignment is validated against double booking, shift overlap and the
 * hard weekly hours cap. The legal weekly reference (48 h) is a warning, not a block, because
 * contracts may legitimately exceed it; the hard cap (66 h) rejects the assignment.
 */
public class AssignScheduleToWorkerService implements AssignScheduleToWorkerUseCase {

  /** Legal weekly reference used for the non-blocking warning. */
  public static final int LEGAL_WEEKLY_HOURS = 48;

  /** Hard weekly cap that rejects an assignment. */
  public static final int HARD_WEEKLY_CAP_HOURS = 66;

  private final ScheduleRepositoryPort scheduleRepository;
  private final WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  private final Logger logger;

  /**
   * Constructs a new AssignScheduleToWorkerService.
   *
   * @param scheduleRepository the schedule repository
   * @param assignmentRepository the assignment repository
   * @param logger the logger
   */
  public AssignScheduleToWorkerService(
      ScheduleRepositoryPort scheduleRepository,
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      Logger logger) {
    this.scheduleRepository = scheduleRepository;
    this.assignmentRepository = assignmentRepository;
    this.logger = logger;
  }

  /**
   * Assigns a schedule to a worker after validating that it exists, that it is not already
   * assigned, that no shift overlaps an existing assignment and that the resulting weekly hours
   * stay under the hard cap.
   *
   * @param info the assignment info containing worker ID and schedule ID
   * @return the created assignment plus the resulting weekly hours and warning
   * @throws ScheduleNotFoundException if the schedule is not found
   * @throws ScheduleAlreadyAssignedException if the schedule is already assigned to the worker
   * @throws ShiftOverlapException if the new shifts overlap with existing assignments
   * @throws WeeklyHoursExceededException if the resulting weekly hours exceed the hard cap
   */
  @Override
  public AssignmentResult assign(AssignInfo info) {
    Schedule schedule =
        scheduleRepository
            .findById(new ScheduleId(info.scheduleId()))
            .orElseThrow(
                () ->
                    new ScheduleNotFoundException(
                        "Schedule not found with id: " + info.scheduleId()));

    List<WorkerScheduleAssignment> existingAssignments =
        assignmentRepository.findByWorkerId(info.workerId());

    boolean alreadyAssigned =
        existingAssignments.stream()
            .anyMatch(assignment -> assignment.getScheduleId().equals(schedule.getId()));
    if (alreadyAssigned) {
      throw new ScheduleAlreadyAssignedException(
          "Schedule "
              + info.scheduleId()
              + " is already assigned to worker "
              + info.workerId().value()
              + " (double booking)");
    }

    List<Schedule> existingSchedules =
        existingAssignments.stream()
            .map(assignment -> scheduleRepository.findById(assignment.getScheduleId()).orElse(null))
            .filter(Objects::nonNull)
            .toList();

    for (ScheduleShift newShift : schedule.getShifts()) {
      for (Schedule existingSchedule : existingSchedules) {
        for (ScheduleShift existingShift : existingSchedule.getShifts()) {
          if (newShift.overlapsWith(existingShift)) {
            throw new ShiftOverlapException(
                "Shift overlap detected",
                "New shift " + newShift + " overlaps with existing shift " + existingShift);
          }
        }
      }
    }

    long totalMinutes =
        existingSchedules.stream().mapToLong(this::weeklyMinutes).sum() + weeklyMinutes(schedule);
    double weeklyHours = totalMinutes / 60.0;
    if (totalMinutes > HARD_WEEKLY_CAP_HOURS * 60L) {
      throw new WeeklyHoursExceededException(
          String.format(
              Locale.ROOT,
              "Assigning schedule %d would raise worker %d to %.2f h/week, above the %d h/week"
                  + " hard cap",
              info.scheduleId(),
              info.workerId().value(),
              weeklyHours,
              HARD_WEEKLY_CAP_HOURS),
          weeklyHours,
          HARD_WEEKLY_CAP_HOURS);
    }

    String warning = null;
    if (totalMinutes > LEGAL_WEEKLY_HOURS * 60L) {
      warning =
          String.format(
              Locale.ROOT,
              "Weekly hours %.2f exceed the %d h legal reference",
              weeklyHours,
              LEGAL_WEEKLY_HOURS);
    }

    WorkerScheduleAssignment assignment =
        new WorkerScheduleAssignment(info.workerId(), schedule.getId());
    WorkerScheduleAssignment saved = assignmentRepository.save(assignment);
    logger.info(
        "Schedule assigned: workerId={}, scheduleId={}, weeklyHours={}",
        info.workerId().value(),
        info.scheduleId(),
        weeklyHours);
    return new AssignmentResult(
        saved, weeklyHours, LEGAL_WEEKLY_HOURS, HARD_WEEKLY_CAP_HOURS, warning);
  }

  private long weeklyMinutes(Schedule schedule) {
    return schedule.getShifts().stream()
        .mapToLong(shift -> Duration.between(shift.startTime(), shift.endTime()).toMinutes())
        .sum();
  }
}
