/* (C) 2026 */
package aros.services.rms.infraestructure.daymenu.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for day menu history. */
@Repository
public interface DayMenuHistoryJpaRepository extends JpaRepository<DayMenuHistoryEntity, Long> {

  /**
   * Finds all history ordered by valid until descending.
   *
   * @param pageable the pagination info
   * @return the page of history entries
   */
  Page<DayMenuHistoryEntity> findAllByOrderByValidUntilDesc(Pageable pageable);
}
