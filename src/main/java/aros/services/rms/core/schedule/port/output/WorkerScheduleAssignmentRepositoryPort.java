package aros.services.rms.core.schedule.port.output;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignmentId;
import aros.services.rms.core.user.domain.UserId;
import java.util.List;

/** Repository port for WorkerScheduleAssignment persistence operations. */
public interface WorkerScheduleAssignmentRepositoryPort {
  /** Persists a worker schedule assignment. */
  WorkerScheduleAssignment save(WorkerScheduleAssignment assignment);

  /** Finds all assignments for a given worker. */
  List<WorkerScheduleAssignment> findByWorkerId(UserId workerId);

  /** Finds all assignments for a given schedule. */
  List<WorkerScheduleAssignment> findByScheduleId(ScheduleId scheduleId);

  /** Finds assignments for a worker on a specific day of week. */
  List<WorkerScheduleAssignment> findByWorkerIdAndDayOfWeek(UserId workerId, DayOfWeek dayOfWeek);

  /** Deletes an assignment by its id. */
  void delete(WorkerScheduleAssignmentId id);

  /** Checks whether any assignment exists for the given schedule. */
  boolean existsByScheduleId(ScheduleId scheduleId);
}
