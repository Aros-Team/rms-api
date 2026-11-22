/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payroll.domain.port.output.AreaLaborCostPort;
import aros.services.rms.core.systemconfig.domain.LaborCostMode;
import aros.services.rms.core.systemconfig.domain.LaborSettings;
import aros.services.rms.core.systemconfig.domain.port.input.GetLaborSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Calculates labor cost per hour for a given area.
 *
 * <p>The {@code labor_cost_mode} setting selects the strategy:
 *
 * <ul>
 *   <li>REAL: payroll net_amount / hours_worked for workers in the area
 *   <li>STANDARD: sum(salary) / sum(expected_hours_per_month) for active workers
 *   <li>AUTO: REAL if payroll exists, otherwise STANDARD
 * </ul>
 *
 * <p>When the selected strategy has no data, the configured {@code labor_standard_hourly_rate} is
 * used as the last fallback.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AreaLaborCostAdapter implements AreaLaborCostPort {

  private final CurrencyProvider currencyProvider;
  private final GetLaborSettingsUseCase getLaborSettingsUseCase;

  @PersistenceContext private EntityManager entityManager;

  @Override
  public Money calculateCostPerHour(Long areaId, YearMonth period) {
    LaborSettings settings = getLaborSettingsUseCase.get();
    if (settings.mode() == LaborCostMode.REAL) {
      return realCost(areaId, period, settings);
    }
    if (settings.mode() == LaborCostMode.STANDARD) {
      return standardCost(areaId, settings);
    }
    Money real = realCost(areaId, period, settings);
    if (real.isPositive()) {
      return real;
    }
    Money standard = standardCost(areaId, settings);
    return standard.isPositive() ? standard : configuredRate(areaId, settings);
  }

  private Money realCost(Long areaId, YearMonth period, LaborSettings settings) {
    List<Object[]> realResult = executeRealQuery(areaId, period);
    if (realResult != null && !realResult.isEmpty()) {
      Object[] row = realResult.get(0);
      BigDecimal totalNet = row[0] != null ? (BigDecimal) row[0] : BigDecimal.ZERO;
      BigDecimal totalHours = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
      if (totalHours.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal rate = totalNet.divide(totalHours, 2, RoundingMode.HALF_UP);
        log.debug(
            "Area {} cost/hour (REAL mode): net={}, hours={}, cost={}",
            areaId,
            totalNet,
            totalHours,
            rate);
        return new Money(rate, currencyProvider.getCurrency());
      }
      log.debug("Payroll exists for area {} but total hours is 0 (REAL mode)", areaId);
    }
    return Money.zero(currencyProvider.getCurrency());
  }

  private Money standardCost(Long areaId, LaborSettings settings) {
    List<Object[]> standardResult = executeStandardQuery(areaId, settings);
    if (standardResult != null && !standardResult.isEmpty()) {
      Object[] row = standardResult.get(0);
      BigDecimal totalSalary = row[0] != null ? (BigDecimal) row[0] : BigDecimal.ZERO;
      BigDecimal totalExpectedHours = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
      if (totalExpectedHours.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal rate = totalSalary.divide(totalExpectedHours, 2, RoundingMode.HALF_UP);
        log.debug(
            "Area {} cost/hour (STANDARD mode): salary={}, hours={}, cost={}",
            areaId,
            totalSalary,
            totalExpectedHours,
            rate);
        return new Money(rate, currencyProvider.getCurrency());
      }
    }
    return Money.zero(currencyProvider.getCurrency());
  }

  private Money configuredRate(Long areaId, LaborSettings settings) {
    if (settings.standardHourlyRate() != null && settings.standardHourlyRate().isPositive()) {
      log.debug(
          "Area {} cost/hour falls back to configured standard rate {}",
          areaId,
          settings.standardHourlyRate().amount());
      return new Money(settings.standardHourlyRate().amount(), currencyProvider.getCurrency());
    }
    log.warn("No workers, payroll or standard rate for area {}, returning zero cost", areaId);
    return Money.zero(currencyProvider.getCurrency());
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
  private List<Object[]> executeStandardQuery(Long areaId, LaborSettings settings) {
    return entityManager
        .createNativeQuery(
            "SELECT SUM(u.salary), SUM(COALESCE(u.expected_hours_per_month, :defaultHours)) "
                + "FROM users u "
                + "JOIN user_assigned_areas uaa ON uaa.user_id = u.id "
                + "WHERE uaa.area_id = :areaId "
                + "  AND u.role = 'WORKER' "
                + "  AND u.deleted_at IS NULL "
                + "  AND u.status = 'ACTIVE' "
                + "  AND u.salary IS NOT NULL "
                + "  AND u.salary > 0")
        .setParameter("areaId", areaId)
        .setParameter("defaultHours", settings.defaultExpectedHoursPerMonth())
        .getResultList();
  }
}
