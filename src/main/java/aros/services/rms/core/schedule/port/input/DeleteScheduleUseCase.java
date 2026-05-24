package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.ScheduleId;

public interface DeleteScheduleUseCase {
  void delete(ScheduleId id);
}
