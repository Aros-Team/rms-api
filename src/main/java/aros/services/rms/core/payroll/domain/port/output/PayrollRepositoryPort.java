/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.output;

import aros.services.rms.core.payroll.domain.Payroll;
import java.util.List;
import java.util.Optional;

/** Output port for payroll persistence operations. */
public interface PayrollRepositoryPort {

  /**
   * Saves a payroll record.
   *
   * @param payroll the payroll to save
   * @return the saved payroll
   */
  Payroll save(Payroll payroll);

  /**
   * Finds a payroll by id.
   *
   * @param id the payroll id
   * @return the payroll if found
   */
  Optional<Payroll> findById(Long id);

  /**
   * Finds a payroll by user and period.
   *
   * @param userId the user id
   * @param year the year
   * @param month the month
   * @return the payroll if found
   */
  Optional<Payroll> findByUserIdAndPeriod(Long userId, int year, int month);

  /**
   * Returns all payroll records.
   *
   * @return list of all payrolls
   */
  List<Payroll> findAll();

  /**
   * Returns payroll records for a given period.
   *
   * @param year the year
   * @param month the month
   * @return list of matching payrolls
   */
  List<Payroll> findByPeriod(int year, int month);

  /**
   * Returns payroll records for a given user.
   *
   * @param userId the user id
   * @return list of matching payrolls
   */
  List<Payroll> findByUserId(Long userId);

  /**
   * Returns payroll records for an area in a given period.
   *
   * @param areaId the area id
   * @param year the year
   * @param month the month
   * @return list of matching payrolls
   */
  List<Payroll> findByAreaIdAndPeriod(Long areaId, int year, int month);

  /**
   * Deletes a payroll record by id.
   *
   * @param id the payroll id
   */
  void deleteById(Long id);

  /**
   * Checks if a payroll exists for a user in a given period.
   *
   * @param userId the user id
   * @param year the year
   * @param month the month
   * @return true if exists
   */
  boolean existsByUserIdAndPeriod(Long userId, int year, int month);
}
