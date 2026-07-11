package aros.services.rms.infraestructure.schedule.persistence.jpa;

import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** JPA repository for WorkerScheduleAssignmentEntity. */
public interface WorkerScheduleAssignmentJpaRepository
    extends JpaRepository<WorkerScheduleAssignmentEntity, Long> {

  /** Finds assignments by worker ID with pessimistic write lock. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT a FROM WorkerScheduleAssignmentEntity a WHERE a.workerId = :workerId")
  List<WorkerScheduleAssignmentEntity> findByWorkerIdWithLock(@Param("workerId") Long workerId);

  /** Finds assignments by worker ID. */
  List<WorkerScheduleAssignmentEntity> findByWorkerId(Long workerId);

  /** Finds assignments by schedule ID. */
  List<WorkerScheduleAssignmentEntity> findByScheduleId(Long scheduleId);

  /** Checks if any assignment exists for the given schedule ID. */
  boolean existsByScheduleId(Long scheduleId);

  /** Deletes assignments by worker and schedule IDs. */
  void deleteByWorkerIdAndScheduleId(Long workerId, Long scheduleId);
}
