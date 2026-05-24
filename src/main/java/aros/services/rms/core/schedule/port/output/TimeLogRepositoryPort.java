package aros.services.rms.core.schedule.port.output;

import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;
import java.util.List;

public interface TimeLogRepositoryPort {
  TimeLog save(TimeLog timeLog);

  List<TimeLog> findByWorkerIdAndDateRange(UserId workerId, Instant from, Instant to);

  List<TimeLog> findAllByDateRange(Instant from, Instant to);
}
