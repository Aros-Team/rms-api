/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.MonthlyFinancialSummary;
import aros.services.rms.core.analytics.domain.port.in.RefreshPrimeCostUseCase;
import aros.services.rms.core.analytics.domain.port.out.MonthlyFinancialSummaryRepositoryPort;
import aros.services.rms.core.common.money.domain.Money;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link RefreshPrimeCostUseCase} that aggregates inventory movements, orders,
 * and time logs for a given date into a {@link MonthlyFinancialSummary} row using native SQL
 * queries.
 */
@Service
@RequiredArgsConstructor
public class RefreshPrimeCostService implements RefreshPrimeCostUseCase {

  private static final Logger log = LoggerFactory.getLogger(RefreshPrimeCostService.class);
  private static final Currency COP = Currency.getInstance("COP");
  private static final BigDecimal HOURS_PER_MONTH = new BigDecimal("160");

  private final EntityManager entityManager;
  private final MonthlyFinancialSummaryRepositoryPort summaryRepo;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public MonthlyFinancialSummary refreshForDate(LocalDate date) {
    String periodKey = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    String bucket = "daily";

    // 1. COGS aggregation per food_type
    Money cogsFood = aggregateCogsForDate(date, "FOOD");
    Money cogsBeverage = aggregateCogsForDate(date, "BEVERAGE");
    Money cogsAlcohol = aggregateCogsForDate(date, "ALCOHOL");
    Money cogsOther = aggregateCogsForDate(date, "OTHER");

    Money totalCogs = cogsFood.plus(cogsBeverage).plus(cogsAlcohol).plus(cogsOther);

    BigDecimal foodCogsPct = computePct(cogsFood, totalCogs);

    // 2. Net sales aggregation
    Money netSales = aggregateNetSalesForDate(date);
    Money grossSales = aggregateGrossSalesForDate(date);

    // 3. Labor aggregation — for MVP, shifts may not have start/end times
    //    Fallback: compute from time_logs × salary/160 if possible, else 0
    Money laborFoh = computeLaborForArea(date, "FOH");
    Money laborBoh = computeLaborForArea(date, "BOH");
    Money laborTotal = laborFoh.plus(laborBoh);
    BigDecimal laborPct = computePct(laborTotal, netSales);

    // 4. Prime cost
    Money primeCost = totalCogs.plus(laborTotal);
    BigDecimal primeCostPct = computePct(primeCost, netSales);

    // 5. Profit margins
    Money grossProfit = netSales.minus(primeCost);
    BigDecimal grossProfitPct = computePct(grossProfit, netSales);
    // For MVP, net profit = gross profit (no other costs modeled)
    BigDecimal netProfitPct = grossProfitPct;

    // 6. Data completeness
    boolean hasAllData = true;
    String completeness = "FULL";
    // If time tracking data is missing, flag as PARTIAL
    if (laborTotal.isZero() && !isLaborDataAvailable(date)) {
      completeness = "PARTIAL";
      hasAllData = false;
    }

    MonthlyFinancialSummary summary =
        MonthlyFinancialSummary.builder()
            .periodKey(periodKey)
            .bucket(bucket)
            .netSales(netSales)
            .grossSales(grossSales)
            .discounts(Money.zero(COP))
            .comped(Money.zero(COP))
            .cogsFood(cogsFood)
            .cogsBeverage(cogsBeverage)
            .cogsAlcohol(cogsAlcohol)
            .cogsOther(cogsOther)
            .foodCogsPct(foodCogsPct)
            .laborFoh(laborFoh)
            .laborBoh(laborBoh)
            .laborTotal(laborTotal)
            .laborPct(laborPct)
            .primeCost(primeCost)
            .primeCostPct(primeCostPct)
            .grossProfitPct(grossProfitPct)
            .netProfitPct(netProfitPct)
            .dataCompleteness(completeness)
            .build();

    summaryRepo.upsert(summary);

    if (!hasAllData) {
      log.warn(
          "Labor data incomplete for {}. Some worker time logs or shifts may be missing.",
          periodKey);
    }

    return summary;
  }

  /**
   * Aggregates COGS for a specific date and food type.
   *
   * <p>COGS = Σ(inventory_movements.quantity × supply_variants.unit_cost) for DEDUCTION movements
   * where the supply's category matches the given food_type.
   */
  private Money aggregateCogsForDate(LocalDate date, String foodType) {
    String sql =
        """
        SELECT COALESCE(SUM(im.quantity * sv.unit_cost), 0)
        FROM inventory_movements im
        JOIN supply_variants sv ON sv.id = im.supply_variant_id
        JOIN supplies s ON s.id = sv.supply_id
        JOIN supply_categories sc ON sc.id = s.supply_category_id
        WHERE im.movement_type = 'DEDUCTION'
          AND sc.food_type = :foodType
          AND im.created_at >= :start
          AND im.created_at < :end
        """;

    Query query =
        entityManager
            .createNativeQuery(sql)
            .setParameter("foodType", foodType)
            .setParameter("start", date.atStartOfDay())
            .setParameter("end", date.plusDays(1).atStartOfDay());

    BigDecimal result = (BigDecimal) query.getSingleResult();
    return new Money(result, COP);
  }

  /**
   * Aggregates net sales for the given date from order_details.
   *
   * <p>Net sales = Σ(order_details.unit_price) for orders in the date range. Since no
   * discount/comped fields exist yet, net = gross = Σ(unit_price).
   */
  private Money aggregateNetSalesForDate(LocalDate date) {
    return aggregateSalesForDate(date);
  }

  private Money aggregateGrossSalesForDate(LocalDate date) {
    return aggregateSalesForDate(date);
  }

  private Money aggregateSalesForDate(LocalDate date) {
    String sql =
        """
        SELECT COALESCE(SUM(od.unit_price), 0)
        FROM order_details od
        JOIN orders o ON o.id = od.order_id
        WHERE o.date >= :start
          AND o.date < :end
        """;

    Query query =
        entityManager
            .createNativeQuery(sql)
            .setParameter("start", date.atStartOfDay())
            .setParameter("end", date.plusDays(1).atStartOfDay());

    Double result = (Double) query.getSingleResult();
    return new Money(BigDecimal.valueOf(result != null ? result : 0.0), COP);
  }

  /**
   * Computes labor cost for a given area group (FOH/BOH) for the date.
   *
   * <p>Labor = Σ(User.salary / 160 × shift_hours) where shift_hours = Duration.between(start,
   * end).toHours() from the shift referenced by time_logs. Uses a subquery per worker to avoid
   * double-counting users with multiple areas in the same group. If shifts have no start/end times,
   * returns 0.
   */
  private Money computeLaborForArea(LocalDate date, String areaGroup) {
    List<String> areaTypes = mapAreaGroupToTypes(areaGroup);
    if (areaTypes.isEmpty()) {
      return Money.zero(COP);
    }

    try {
      // Subquery: compute labor per worker per shift, then sum and filter by area group
      // Uses user_assigned_areas join table for the many-to-many user-area relationship
      String sql =
          """
          SELECT COALESCE(SUM(labor_by_worker.labor_cost), 0)
          FROM (
            SELECT DISTINCT tl.worker_id,
              (u.salary / :hoursPerMonth) *
              TIMESTAMPDIFF(SECOND, ss.start_time, ss.end_time) / 3600 AS labor_cost
            FROM time_logs tl
            JOIN users u ON u.id = tl.worker_id
            JOIN schedule_shifts ss ON ss.id = tl.related_shift_id
            WHERE tl.timestamp >= :start
              AND tl.timestamp < :end
              AND tl.type = 'IN'
              AND u.salary IS NOT NULL
              AND u.salary > 0
          ) labor_by_worker
          JOIN user_assigned_areas uaa ON uaa.user_id = labor_by_worker.worker_id
          JOIN areas a ON a.id = uaa.area_id
          WHERE a.type IN (:areaTypes)
          """;

      Query query =
          entityManager
              .createNativeQuery(sql)
              .setParameter("hoursPerMonth", HOURS_PER_MONTH)
              .setParameter("start", date.atStartOfDay())
              .setParameter("end", date.plusDays(1).atStartOfDay())
              .setParameter("areaTypes", areaTypes);

      BigDecimal result = (BigDecimal) query.getSingleResult();
      return new Money(result, COP);
    } catch (Exception e) {
      log.debug(
          "Shift-based labor aggregation failed for {}: {}. Falling back to 0.",
          areaGroup,
          e.getMessage());
      return Money.zero(COP);
    }
  }

  /**
   * Checks if labor data exists for the given date (time_logs with related shifts).
   *
   * @return true if at least one time_log with a related shift exists for the date
   */
  private boolean isLaborDataAvailable(LocalDate date) {
    try {
      String sql =
          """
          SELECT COUNT(1)
          FROM time_logs tl
          JOIN users u ON u.id = tl.worker_id
          JOIN schedule_shifts ss ON ss.id = tl.related_shift_id
          WHERE tl.timestamp >= :start
            AND tl.timestamp < :end
            AND tl.type = 'IN'
            AND u.salary IS NOT NULL
            AND u.salary > 0
          LIMIT 1
          """;

      Query query =
          entityManager
              .createNativeQuery(sql)
              .setParameter("start", date.atStartOfDay())
              .setParameter("end", date.plusDays(1).atStartOfDay());

      Number count = (Number) query.getSingleResult();
      return count != null && count.longValue() > 0;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Maps an area group (FOH/BOH) to the corresponding AreaType enum values.
   *
   * @param areaGroup "FOH" or "BOH"
   * @return list of area type names
   */
  private List<String> mapAreaGroupToTypes(String areaGroup) {
    if ("FOH".equalsIgnoreCase(areaGroup)) {
      return List.of("SERVICE", "CASH", "BAR");
    } else if ("BOH".equalsIgnoreCase(areaGroup)) {
      return List.of("KITCHEN", "GRILL");
    }
    return List.of();
  }

  private static BigDecimal computePct(Money numerator, Money denominator) {
    if (denominator.isZero()) {
      return BigDecimal.ZERO;
    }
    return numerator
        .amount()
        .multiply(new BigDecimal("100"))
        .divide(denominator.amount(), 2, java.math.RoundingMode.HALF_UP);
  }
}
