package aros.services.rms.infraestructure.schedule.persistence.jpa;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignmentId;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
@RequiredArgsConstructor
public class WorkerScheduleAssignmentRepositoryAdapter
    implements WorkerScheduleAssignmentRepositoryPort {

  private final WorkerScheduleAssignmentJpaRepository internal;
  private final WorkerScheduleAssignmentMapper mapper;

  @Override
  public WorkerScheduleAssignment save(WorkerScheduleAssignment assignment) {
    WorkerScheduleAssignmentEntity entity = mapper.toEntity(assignment);
    WorkerScheduleAssignmentEntity saved = internal.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public List<WorkerScheduleAssignment> findByWorkerId(UserId workerId) {
    return internal.findByWorkerId(workerId.value()).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<WorkerScheduleAssignment> findByScheduleId(ScheduleId scheduleId) {
    return internal.findByScheduleId(scheduleId.value()).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<WorkerScheduleAssignment> findByWorkerIdAndDayOfWeek(
      UserId workerId, DayOfWeek dayOfWeek) {
    return findByWorkerId(workerId);
  }

  @Override
  public void delete(WorkerScheduleAssignmentId id) {
    internal.deleteById(id.value());
  }

  @Override
  public boolean existsByScheduleId(ScheduleId scheduleId) {
    return internal.existsByScheduleId(scheduleId.value());
  }
}
