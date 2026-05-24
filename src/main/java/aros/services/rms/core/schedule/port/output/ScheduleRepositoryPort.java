package aros.services.rms.core.schedule.port.output;

import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepositoryPort {
  Schedule save(Schedule schedule);

  Optional<Schedule> findById(ScheduleId id);

  List<Schedule> findAll();

  Optional<Schedule> findByName(String name);

  boolean existsByName(String name);

  void delete(ScheduleId id);
}
