package aros.services.rms.infraestructure.schedule.persistence.jpa;

import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import jakarta.transaction.Transactional;
import java.util.List;
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

  /** {@inheritDoc} */
  @Override
  public Schedule save(Schedule schedule) {
    ScheduleEntity entity = mapper.toEntity(schedule);
    if (schedule.getId() != null && schedule.getId().value() != null) {
      entity.setId(schedule.getId().value());
    }
    List<ScheduleShiftEntity> shiftEntities =
        schedule.getShifts().stream()
            .map(
                shift -> {
                  ScheduleShiftEntity se = new ScheduleShiftEntity();
                  se.setDayOfWeek(shift.dayOfWeek());
                  se.setStartTime(shift.startTime());
                  se.setEndTime(shift.endTime());
                  return se;
                })
            .toList();
    entity.getShifts().clear();
    entity.getShifts().addAll(shiftEntities);
    entity.getShifts().forEach(se -> se.setSchedule(entity));

    ScheduleEntity saved = internal.save(entity);
    return mapper.toDomain(saved);
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
