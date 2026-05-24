package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.user.domain.UserId;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface GetWorkerShiftsUseCase {
  Map<DayOfWeek, List<ShiftDetail>> getShifts(UserId workerId);

  record ShiftDetail(String scheduleName, LocalTime startTime, LocalTime endTime) {}
}
