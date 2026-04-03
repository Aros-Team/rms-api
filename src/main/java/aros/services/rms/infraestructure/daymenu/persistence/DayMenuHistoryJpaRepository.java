/* (C) 2026 */
package aros.services.rms.infraestructure.daymenu.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayMenuHistoryJpaRepository extends JpaRepository<DayMenuHistoryEntity, Long> {

  Page<DayMenuHistoryEntity> findAllByOrderByValidUntilDesc(Pageable pageable);
}
