package aros.services.rms.infraestructure.schedule.persistence.jpa;

import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkerScheduleAssignmentJpaRepository
    extends JpaRepository<WorkerScheduleAssignmentEntity, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT a FROM WorkerScheduleAssignmentEntity a WHERE a.workerId = :workerId")
  List<WorkerScheduleAssignmentEntity> findByWorkerIdWithLock(@Param("workerId") Long workerId);

  List<WorkerScheduleAssignmentEntity> findByWorkerId(Long workerId);

  List<WorkerScheduleAssignmentEntity> findByScheduleId(Long scheduleId);

  boolean existsByScheduleId(Long scheduleId);

  void deleteByWorkerIdAndScheduleId(Long workerId, Long scheduleId);
}
