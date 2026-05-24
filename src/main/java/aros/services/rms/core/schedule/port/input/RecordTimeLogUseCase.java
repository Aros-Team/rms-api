package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.user.domain.UserId;

public interface RecordTimeLogUseCase {
  RecordTimeLogResult execute(UserId workerId);

  record RecordTimeLogResult(boolean withinShift, ScheduleShift activeShift) {}
}
