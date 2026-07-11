/* (C) 2026 */

package aros.services.rms.infraestructure.user.persistence.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for salary history entities. */
public interface SalaryHistoryJpaRepository extends JpaRepository<SalaryHistoryEntity, Long> {

  /**
   * Finds all salary history entries for a user ordered by most recent first.
   *
   * @param userId the user identifier
   * @return list of salary history entities
   */
  List<SalaryHistoryEntity> findByUserIdOrderByChangedAtDesc(Long userId);
}
