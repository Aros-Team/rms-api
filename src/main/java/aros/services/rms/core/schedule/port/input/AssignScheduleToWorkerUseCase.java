/* (C) 2026 */

package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.user.domain.UserId;

/** Use case for assigning a schedule to a worker. */
public interface AssignScheduleToWorkerUseCase {
  /**
   * Assigns a schedule to a worker and returns the assignment.
   *
   * @param info the worker and schedule to assign
   * @return the created assignment and the resulting weekly hours
   */
  AssignmentResult assign(AssignInfo info);

  /**
   * Information required to assign a schedule to a worker.
   *
   * @param workerId the identifier of the worker
   * @param scheduleId the identifier of the schedule
   */
  record AssignInfo(UserId workerId, Long scheduleId) {}

  /**
   * Result of a schedule assignment.
   *
   * @param assignment the persisted assignment
   * @param weeklyHours the worker's resulting weekly hours
   * @param legalWeeklyHours the legal weekly reference
   * @param hardWeeklyCapHours the hard weekly cap
   * @param warning a non-blocking warning, or null when weekly hours are at or below the legal
   *     reference
   */
  record AssignmentResult(
      WorkerScheduleAssignment assignment,
      double weeklyHours,
      int legalWeeklyHours,
      int hardWeeklyCapHours,
      String warning) {}
}
