/* (C) 2026 */
package aros.services.rms.infraestructure.daymenu.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for day menu. */
@Repository
public interface DayMenuJpaRepository extends JpaRepository<DayMenuEntity, Long> {

  /**
   * Finds the first day menu.
   *
   * @return the first day menu if exists
   */
  Optional<DayMenuEntity> findFirstBy();
}
