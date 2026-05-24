package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.WorkerScheduleAssignmentId;

public interface RemoveScheduleFromWorkerUseCase {
  void remove(WorkerScheduleAssignmentId assignmentId);
}
