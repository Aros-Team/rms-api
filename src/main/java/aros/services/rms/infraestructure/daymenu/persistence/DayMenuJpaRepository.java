/* (C) 2026 */
package aros.services.rms.infraestructure.daymenu.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayMenuJpaRepository extends JpaRepository<DayMenuEntity, Long> {

  Optional<DayMenuEntity> findFirstBy();
}
