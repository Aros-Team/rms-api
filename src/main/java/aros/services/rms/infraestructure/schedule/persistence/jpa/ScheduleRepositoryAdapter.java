package aros.services.rms.infraestructure.schedule.persistence.jpa;

import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Repository adapter for Schedule persistence. */
@Repository
@Transactional
@RequiredArgsConstructor
public class ScheduleRepositoryAdapter implements ScheduleRepositoryPort {

  private final ScheduleJpaRepository internal;
  private final ScheduleMapper mapper;

  /**
   * {@inheritDoc}
   *
   * <p>Persists a schedule, reusing the rows of existing shifts whenever possible. Replacing every
   * shift row would orphan {@code time_logs.related_shift_id}, whose foreign key blocks the delete.
   * Matching by day and the closest existing times keeps the row (and its id) for edits, so a moved
   * or resized shift does not lose its attendance references.
   */
  @Override
  public Schedule save(Schedule schedule) {
    ScheduleEntity entity =
        schedule.getId() != null && schedule.getId().value() != null
            ? internal.findById(schedule.getId().value()).orElseGet(() -> mapper.toEntity(schedule))
            : mapper.toEntity(schedule);

    entity.setName(schedule.getName());
    entity.setDescription(schedule.getDescription());
    reconcileShifts(entity, schedule.getShifts());

    ScheduleEntity saved = internal.save(entity);
    return mapper.toDomain(saved);
  }

  /**
   * Updates the entity's shifts in place, adding only genuinely new rows and removing leftovers.
   *
   * <p>Matching passes: exact day/time, same day and start, same day and end, then the first unused
   * row of that day. The last pass preserves ids when both edges move.
   */
  private void reconcileShifts(ScheduleEntity entity, List<ScheduleShift> shifts) {
    List<ScheduleShiftEntity> current = entity.getShifts();
    boolean[] used = new boolean[current.size()];
    List<ScheduleShiftEntity> next = new ArrayList<>(shifts.size());

    for (ScheduleShift shift : shifts) {
      int match = matchShift(current, used, shift);
      ScheduleShiftEntity target;
      if (match >= 0) {
        used[match] = true;
        target = current.get(match);
      } else {
        target = new ScheduleShiftEntity();
        target.setSchedule(entity);
      }
      target.setDayOfWeek(shift.dayOfWeek());
      target.setStartTime(shift.startTime());
      target.setEndTime(shift.endTime());
      next.add(target);
    }

    current.removeIf(existing -> !next.contains(existing));
    for (ScheduleShiftEntity target : next) {
      if (!current.contains(target)) {
        current.add(target);
      }
    }
  }

  private int matchShift(
      List<ScheduleShiftEntity> current, boolean[] used, ScheduleShift candidate) {
    for (int pass = 0; pass < 4; pass++) {
      for (int index = 0; index < current.size(); index++) {
        if (used[index]) {
          continue;
        }
        ScheduleShiftEntity existing = current.get(index);
        if (existing.getDayOfWeek() != candidate.dayOfWeek()) {
          continue;
        }
        boolean sameStart = Objects.equals(existing.getStartTime(), candidate.startTime());
        boolean sameEnd = Objects.equals(existing.getEndTime(), candidate.endTime());
        if (pass == 0 && sameStart && sameEnd
            || pass == 1 && sameStart
            || pass == 2 && sameEnd
            || pass == 3) {
          return index;
        }
      }
    }
    return -1;
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Schedule> findById(ScheduleId id) {
    return internal.findById(id.value()).map(mapper::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  public List<Schedule> findAll() {
    return internal.findAll().stream().map(mapper::toDomain).toList();
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Schedule> findByName(String name) {
    return internal.findByName(name).map(mapper::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  public boolean existsByName(String name) {
    return internal.existsByName(name);
  }

  /** {@inheritDoc} */
  @Override
  public void delete(ScheduleId id) {
    internal.deleteById(id.value());
  }
}
