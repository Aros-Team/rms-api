package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import java.util.List;

public interface UpdateScheduleUseCase {
  Schedule update(ScheduleId id, UpdateScheduleInfo info);

  record UpdateScheduleInfo(String name, String description, List<ShiftInfo> shifts) {
    public record ShiftInfo(String dayOfWeek, String startTime, String endTime) {}
  }
}
