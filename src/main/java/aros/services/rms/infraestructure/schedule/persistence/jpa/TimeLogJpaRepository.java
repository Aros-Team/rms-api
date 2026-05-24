package aros.services.rms.infraestructure.schedule.persistence.jpa;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeLogJpaRepository extends JpaRepository<TimeLogEntity, Long> {

  List<TimeLogEntity> findByWorkerIdAndTimestampBetween(Long workerId, Instant from, Instant to);

  List<TimeLogEntity> findByTimestampBetween(Instant from, Instant to);
}
