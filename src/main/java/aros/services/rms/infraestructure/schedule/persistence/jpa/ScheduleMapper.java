package aros.services.rms.infraestructure.schedule.persistence.jpa;

import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/** MapStruct mapper between ScheduleEntity and Schedule. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ScheduleMapper {

  /** Maps a ScheduleEntity to a domain Schedule. */
  public Schedule toDomain(ScheduleEntity entity) {
    if (entity == null) {
      return null;
    }
    List<ScheduleShift> shifts =
        entity.getShifts().stream()
            .map(
                e ->
                    new ScheduleShift(
                        e.getId(), e.getDayOfWeek(), e.getStartTime(), e.getEndTime()))
            .toList();
    return new Schedule(
        ScheduleId.of(entity.getId()), entity.getName(), entity.getDescription(), shifts);
  }

  /** Maps a domain Schedule to a ScheduleEntity. */
  @Mapping(source = "id.value", target = "id")
  @Mapping(target = "shifts", ignore = true)
  public abstract ScheduleEntity toEntity(Schedule domain);
}
