package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.user.domain.UserId;

public interface AssignScheduleToWorkerUseCase {
  WorkerScheduleAssignment assign(AssignInfo info);

  record AssignInfo(UserId workerId, Long scheduleId) {}
}
