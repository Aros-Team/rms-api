package aros.services.rms.infraestructure.schedule.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleJpaRepository extends JpaRepository<ScheduleEntity, Long> {
  boolean existsByName(String name);

  Optional<ScheduleEntity> findByName(String name);
}
