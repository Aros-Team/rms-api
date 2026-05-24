package aros.services.rms.infraestructure.schedule.persistence.jpa;

import aros.services.rms.core.schedule.domain.LogType;
import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.domain.TimeLogId;
import aros.services.rms.core.user.domain.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class TimeLogMapper {

  public TimeLog toDomain(TimeLogEntity entity) {
    if (entity == null) return null;
    return new TimeLog(
        TimeLogId.of(entity.getId()),
        UserId.of(entity.getWorkerId()),
        entity.getTimestamp(),
        LogType.valueOf(entity.getType()),
        entity.isWithinShift(),
        entity.getRelatedShiftId());
  }

  @Mapping(source = "id.value", target = "id")
  @Mapping(source = "workerId.value", target = "workerId")
  @Mapping(source = "type", target = "type")
  public abstract TimeLogEntity toEntity(TimeLog domain);
}
