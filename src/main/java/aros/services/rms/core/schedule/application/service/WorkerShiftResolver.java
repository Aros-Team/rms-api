/* (C) 2026 */

package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Resolves the recurring weekly shifts assigned to a worker and evaluates whether an instant falls
 * inside one of them.
 *
 * <p>The current schedule model has no effective dates: an assignment applies to every occurrence
 * of its weekday. This resolver therefore uses the worker's local day and time in the business
 * timezone.
 */
public class WorkerShiftResolver {

  private final WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  private final ScheduleRepositoryPort scheduleRepository;
  private final ZoneId zoneId;

  /**
   * Constructs a new WorkerShiftResolver.
   *
   * @param assignmentRepository the assignment repository
   * @param scheduleRepository the schedule repository
   * @param zoneId the business timezone
   */
  public WorkerShiftResolver(
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      ScheduleRepositoryPort scheduleRepository,
      ZoneId zoneId) {
    this.assignmentRepository = assignmentRepository;
    this.scheduleRepository = scheduleRepository;
    this.zoneId = zoneId;
  }

  /**
   * Returns all weekly shifts assigned to the worker.
   *
   * @param workerId the worker identifier
   * @return a flat list of the worker's shifts across all assigned schedules
   */
  public List<ScheduleShift> shiftsFor(UserId workerId) {
    return assignmentRepository.findByWorkerId(workerId).stream()
        .map(assignment -> scheduleRepository.findById(assignment.getScheduleId()).orElse(null))
        .filter(Objects::nonNull)
        .flatMap(schedule -> schedule.getShifts().stream())
        .toList();
  }

  /**
   * Finds the shift that covers the given instant for the worker.
   *
   * @param workerId the worker identifier
   * @param at the instant to evaluate
   * @return the covering shift, if any
   */
  public Optional<ScheduleShift> activeShift(UserId workerId, Instant at) {
    return activeShift(shiftsFor(workerId), at);
  }

  /**
   * Finds the shift that covers the given instant in an already resolved shift list.
   *
   * @param shifts the worker's shifts
   * @param at the instant to evaluate
   * @return the covering shift, if any
   */
  public Optional<ScheduleShift> activeShift(List<ScheduleShift> shifts, Instant at) {
    ZonedDateTime local = at.atZone(zoneId);
    DayOfWeek day = DayOfWeek.valueOf(local.getDayOfWeek().name());
    LocalTime time = local.toLocalTime();

    return shifts.stream()
        .filter(
            shift ->
                shift.dayOfWeek() == day
                    && !time.isBefore(shift.startTime())
                    && time.isBefore(shift.endTime()))
        .findFirst();
  }

  /**
   * Returns whether the given instant falls inside one of the provided shifts.
   *
   * @param shifts the worker's shifts
   * @param at the instant to evaluate
   * @return true when a shift covers the instant
   */
  public boolean isWithinShift(List<ScheduleShift> shifts, Instant at) {
    return activeShift(shifts, at).isPresent();
  }

  /**
   * Returns the business timezone used for day and time evaluation.
   *
   * @return the business timezone
   */
  public ZoneId getZoneId() {
    return zoneId;
  }
}
