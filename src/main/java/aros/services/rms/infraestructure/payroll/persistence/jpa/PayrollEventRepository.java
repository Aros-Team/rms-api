/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** JPA repository for PayrollEventEntity persistence. */
@Repository
public interface PayrollEventRepository extends JpaRepository<PayrollEventEntity, Long> {

  /**
   * Finds events for a user within a date range.
   *
   * @param userId the user id
   * @param startDate start of the period (inclusive)
   * @param endDate end of the period (inclusive)
   * @return list of matching events
   */
  List<PayrollEventEntity> findByUserIdAndEventDateBetween(
      Long userId, LocalDate startDate, LocalDate endDate);

  /**
   * Finds events for a user within a date range, ordered by event date.
   *
   * @param userId the user id
   * @param startDate start of the period (inclusive)
   * @param endDate end of the period (inclusive)
   * @return list of matching events ordered by event date
   */
  @Query(
      "SELECT e FROM PayrollEventEntity e WHERE e.userId = :userId "
          + "AND e.eventDate BETWEEN :startDate AND :endDate ORDER BY e.eventDate")
  List<PayrollEventEntity> findByUserAndPeriod(
      @Param("userId") Long userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}
