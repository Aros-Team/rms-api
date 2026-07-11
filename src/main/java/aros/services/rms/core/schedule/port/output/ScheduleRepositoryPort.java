package aros.services.rms.core.schedule.port.output;

import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import java.util.List;
import java.util.Optional;

/** Repository port for Schedule persistence operations. */
public interface ScheduleRepositoryPort {
  /** Persists a schedule. */
  Schedule save(Schedule schedule);

  /** Finds a schedule by its id. */
  Optional<Schedule> findById(ScheduleId id);

  /** Returns all schedules. */
  List<Schedule> findAll();

  /** Finds a schedule by its name. */
  Optional<Schedule> findByName(String name);

  /** Checks whether a schedule with the given name exists. */
  boolean existsByName(String name);

  /** Deletes a schedule by its id. */
  void delete(ScheduleId id);
}
