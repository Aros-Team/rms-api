package aros.services.rms.infraestructure.schedule.persistence.jpa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for TimeLogEntity. */
public interface TimeLogJpaRepository extends JpaRepository<TimeLogEntity, Long> {

  /** Finds the most recent time log for a worker. */
  Optional<TimeLogEntity> findTopByWorkerIdOrderByTimestampDescIdDesc(Long workerId);

  /** Finds time logs by worker ID and date range. */
  List<TimeLogEntity> findByWorkerIdAndTimestampBetween(Long workerId, Instant from, Instant to);

  /** Finds time logs within a date range. */
  List<TimeLogEntity> findByTimestampBetween(Instant from, Instant to);
}
