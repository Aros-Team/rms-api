package aros.services.rms.infraestructure.schedule.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for ScheduleEntity. */
public interface ScheduleJpaRepository extends JpaRepository<ScheduleEntity, Long> {
  /** Checks if a schedule with the given name exists. */
  boolean existsByName(String name);

  /** Finds a schedule by its name. */
  Optional<ScheduleEntity> findByName(String name);
}
