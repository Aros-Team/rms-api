package aros.services.rms.infraestructure.schedule.persistence.jpa;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for TimeLogEntity. */
public interface TimeLogJpaRepository extends JpaRepository<TimeLogEntity, Long> {

  /** Finds time logs by worker ID and date range. */
  List<TimeLogEntity> findByWorkerIdAndTimestampBetween(Long workerId, Instant from, Instant to);

  /** Finds time logs within a date range. */
  List<TimeLogEntity> findByTimestampBetween(Instant from, Instant to);
}
