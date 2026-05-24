package aros.services.rms.infraestructure.schedule.persistence.jpa;

import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ScheduleMapper {

  public Schedule toDomain(ScheduleEntity entity) {
    if (entity == null) return null;
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

  @Mapping(source = "id.value", target = "id")
  @Mapping(target = "shifts", ignore = true)
  public abstract ScheduleEntity toEntity(Schedule domain);
}
