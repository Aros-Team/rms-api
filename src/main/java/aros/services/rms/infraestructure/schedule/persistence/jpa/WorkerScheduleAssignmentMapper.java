package aros.services.rms.infraestructure.schedule.persistence.jpa;

import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignmentId;
import aros.services.rms.core.user.domain.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/** MapStruct mapper between WorkerScheduleAssignmentEntity and WorkerScheduleAssignment. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class WorkerScheduleAssignmentMapper {

  /** Maps a WorkerScheduleAssignmentEntity to a domain WorkerScheduleAssignment. */
  public WorkerScheduleAssignment toDomain(WorkerScheduleAssignmentEntity entity) {
    if (entity == null) {
      return null;
    }
    return new WorkerScheduleAssignment(
        WorkerScheduleAssignmentId.of(entity.getId()),
        UserId.of(entity.getWorkerId()),
        ScheduleId.of(entity.getScheduleId()),
        entity.getAssignedAt());
  }

  /** Maps a domain WorkerScheduleAssignment to a WorkerScheduleAssignmentEntity. */
  @Mapping(source = "id.value", target = "id")
  @Mapping(source = "workerId.value", target = "workerId")
  @Mapping(source = "scheduleId.value", target = "scheduleId")
  public abstract WorkerScheduleAssignmentEntity toEntity(WorkerScheduleAssignment domain);
}
