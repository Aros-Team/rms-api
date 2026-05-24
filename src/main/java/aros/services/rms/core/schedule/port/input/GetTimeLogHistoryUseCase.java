package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;
import java.util.List;

public interface GetTimeLogHistoryUseCase {
  List<TimeLog> getHistory(TimeLogFilter filter);

  record TimeLogFilter(UserId workerId, Instant from, Instant to, Boolean withinShift) {}
}
