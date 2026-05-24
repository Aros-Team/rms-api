package aros.services.rms.core.schedule.port.output;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignmentId;
import aros.services.rms.core.user.domain.UserId;
import java.util.List;

public interface WorkerScheduleAssignmentRepositoryPort {
  WorkerScheduleAssignment save(WorkerScheduleAssignment assignment);

  List<WorkerScheduleAssignment> findByWorkerId(UserId workerId);

  List<WorkerScheduleAssignment> findByScheduleId(ScheduleId scheduleId);

  List<WorkerScheduleAssignment> findByWorkerIdAndDayOfWeek(UserId workerId, DayOfWeek dayOfWeek);

  void delete(WorkerScheduleAssignmentId id);

  boolean existsByScheduleId(ScheduleId scheduleId);
}
