/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import aros.services.rms.core.systemconfig.domain.OpeningHoursDay;
import aros.services.rms.core.systemconfig.domain.port.output.OpeningHoursPort;
import java.time.DayOfWeek;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adapter for the weekly opening hours table. */
@Component
@RequiredArgsConstructor
public class OpeningHoursAdapter implements OpeningHoursPort {

  private final OpeningHoursJpaRepository repository;

  /** {@inheritDoc} */
  @Override
  public List<OpeningHoursDay> findAll() {
    return repository.findAll().stream().map(this::toDomain).toList();
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void replaceAll(List<OpeningHoursDay> days) {
    List<OpeningHoursEntity> entities = days.stream().map(this::toEntity).toList();
    repository.saveAll(entities);
  }

  private OpeningHoursDay toDomain(OpeningHoursEntity entity) {
    return OpeningHoursDay.builder()
        .dayOfWeek(DayOfWeek.valueOf(entity.getDayOfWeek()))
        .openTime(entity.getOpenTime())
        .closeTime(entity.getCloseTime())
        .closed(entity.isClosed())
        .build();
  }

  private OpeningHoursEntity toEntity(OpeningHoursDay day) {
    OpeningHoursEntity entity = new OpeningHoursEntity();
    entity.setDayOfWeek(day.getDayOfWeek().name());
    entity.setOpenTime(day.isClosed() ? null : day.getOpenTime());
    entity.setCloseTime(day.isClosed() ? null : day.getCloseTime());
    entity.setClosed(day.isClosed());
    return entity;
  }
}
