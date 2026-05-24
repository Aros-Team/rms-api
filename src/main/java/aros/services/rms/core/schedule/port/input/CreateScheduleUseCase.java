package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.Schedule;
import java.util.List;

public interface CreateScheduleUseCase {
  Schedule create(CreateScheduleInfo info);

  record CreateScheduleInfo(String name, String description, List<ShiftInfo> shifts) {
    public record ShiftInfo(String dayOfWeek, String startTime, String endTime) {}
  }
}
