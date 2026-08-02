/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** JPA repository for PayrollEntity persistence. */
@Repository
public interface PayrollRepository extends JpaRepository<PayrollEntity, Long> {

  /**
   * Finds a payroll by user and period.
   *
   * @param userId the user id
   * @param year the period year
   * @param month the period month
   * @return the payroll if found
   */
  Optional<PayrollEntity> findByUserIdAndPeriodYearAndPeriodMonth(Long userId, int year, int month);

  /**
   * Finds payroll records for a given period.
   *
   * @param year the period year
   * @param month the period month
   * @return list of matching payrolls
   */
  List<PayrollEntity> findByPeriodYearAndPeriodMonth(int year, int month);

  /**
   * Finds payroll records for a given user.
   *
   * @param userId the user id
   * @return list of matching payrolls
   */
  List<PayrollEntity> findByUserId(Long userId);

  /**
   * Checks if a payroll exists for a user in a given period.
   *
   * @param userId the user id
   * @param year the period year
   * @param month the period month
   * @return true if exists
   */
  boolean existsByUserIdAndPeriodYearAndPeriodMonth(Long userId, int year, int month);

  /**
   * Finds payroll records for an area in a given period. Joins users and assigned areas to find
   * workers in the specified area with PAID or ACCRUED status.
   *
   * @param areaId the area id
   * @param year the period year
   * @param month the period month
   * @return list of matching payrolls
   */
  @Query(
      value =
          "SELECT p.* FROM payroll p "
              + "JOIN users u ON p.user_id = u.id "
              + "JOIN user_assigned_areas uaa ON uaa.user_id = u.id "
              + "WHERE uaa.area_id = :areaId "
              + "AND p.period_year = :year AND p.period_month = :month "
              + "AND p.status IN ('PAID', 'ACCRUED') "
              + "AND u.role = 'WORKER'",
      nativeQuery = true)
  List<PayrollEntity> findByAreaIdAndPeriod(
      @Param("areaId") Long areaId, @Param("year") int year, @Param("month") int month);
}
