/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.port.output.AreaLaborCostPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Calculates labor cost per hour for a given area.
 *
 * <p>Modes:
 *
 * <ul>
 *   <li>REAL: payroll net_amount / hours_worked for workers in the area
 *   <li>STANDARD: sum(salary) / sum(expected_hours_per_month) for active workers
 *   <li>AUTO: REAL if payroll exists, otherwise STANDARD
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AreaLaborCostAdapter implements AreaLaborCostPort {

  private static final Currency COP = Currency.getInstance("COP");

  @PersistenceContext private EntityManager entityManager;

  @Override
  public Money calculateCostPerHour(Long areaId, YearMonth period) {
    List<Object[]> realResult = executeRealQuery(areaId, period);
    if (realResult != null && !realResult.isEmpty()) {
      Object[] row = realResult.get(0);
      BigDecimal totalNet = row[0] != null ? (BigDecimal) row[0] : BigDecimal.ZERO;
      BigDecimal totalHours = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;

      if (totalHours.compareTo(BigDecimal.ZERO) > 0) {
        log.debug(
            "Area {} cost/hour (REAL mode): net={}, hours={}, cost={}",
            areaId,
            totalNet,
            totalHours,
            totalNet.divide(totalHours, 2, RoundingMode.HALF_UP));
        return new Money(totalNet.divide(totalHours, 2, RoundingMode.HALF_UP), COP);
      }
      log.warn("Payroll exists for area {} but total hours is 0, falling back to STANDARD", areaId);
    }

    List<Object[]> standardResult = executeStandardQuery(areaId);
    if (standardResult != null && !standardResult.isEmpty()) {
      Object[] row = standardResult.get(0);
      BigDecimal totalSalary = row[0] != null ? (BigDecimal) row[0] : BigDecimal.ZERO;
      BigDecimal totalExpectedHours = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;

      if (totalExpectedHours.compareTo(BigDecimal.ZERO) > 0) {
        log.debug(
            "Area {} cost/hour (STANDARD mode): salary={}, hours={}, cost={}",
            areaId,
            totalSalary,
            totalExpectedHours,
            totalSalary.divide(totalExpectedHours, 2, RoundingMode.HALF_UP));
        return new Money(totalSalary.divide(totalExpectedHours, 2, RoundingMode.HALF_UP), COP);
      }
    }

    log.warn("No workers or payroll data for area {}, returning zero cost", areaId);
    return Money.zero(COP);
  }

  @SuppressWarnings("unchecked")
  private List<Object[]> executeRealQuery(Long areaId, YearMonth period) {
    return entityManager
        .createNativeQuery(
            "SELECT SUM(p.net_amount), SUM(p.hours_worked) "
                + "FROM payroll p "
                + "JOIN users u ON u.id = p.user_id "
                + "JOIN user_assigned_areas uaa ON uaa.user_id = u.id "
                + "WHERE uaa.area_id = :areaId "
                + "  AND p.period_year = :year "
                + "  AND p.period_month = :month "
                + "  AND p.status IN ('PAID', 'ACCRUED') "
                + "  AND u.role = 'WORKER' "
                + "  AND u.deleted_at IS NULL "
                + "  AND p.hours_worked > 0")
        .setParameter("areaId", areaId)
        .setParameter("year", period.getYear())
        .setParameter("month", period.getMonthValue())
        .getResultList();
  }

  @SuppressWarnings("unchecked")
  private List<Object[]> executeStandardQuery(Long areaId) {
    return entityManager
        .createNativeQuery(
            "SELECT SUM(u.salary), SUM(u.expected_hours_per_month) "
                + "FROM users u "
                + "JOIN user_assigned_areas uaa ON uaa.user_id = u.id "
                + "WHERE uaa.area_id = :areaId "
                + "  AND u.role = 'WORKER' "
                + "  AND u.deleted_at IS NULL "
                + "  AND u.status = 'ACTIVE' "
                + "  AND u.salary IS NOT NULL "
                + "  AND u.salary > 0")
        .setParameter("areaId", areaId)
        .getResultList();
  }
}
