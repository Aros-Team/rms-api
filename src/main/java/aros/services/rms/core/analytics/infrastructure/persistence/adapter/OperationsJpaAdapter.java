/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.adapter;

import aros.services.rms.core.analytics.domain.port.out.OperationsQueryPort;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Native-SQL adapter for the operations report aggregates. */
@Component
@RequiredArgsConstructor
public class OperationsJpaAdapter implements OperationsQueryPort {

  private final EntityManager entityManager;
  private final CurrencyProvider currencyProvider;

  /** {@inheritDoc} */
  @Override
  public OperationsSnapshot load(
      LocalDate startInclusive,
      LocalDate endExclusive,
      LocalTime lunchStart,
      LocalTime lunchEnd,
      LocalTime dinnerStart,
      LocalTime dinnerEnd) {
    Currency currency = currencyProvider.getCurrency();
    Money netSales = loadNetSales(startInclusive, endExclusive);

    String aggregateSql =
        """
        SELECT
          COUNT(*) AS total_orders,
          COALESCE(SUM(CASE WHEN o.open_time IS NOT NULL AND o.close_time IS NOT NULL
                            THEN 1 ELSE 0 END), 0) AS orders_with_timestamps,
          COALESCE(SUM(CASE WHEN o.open_time IS NOT NULL AND o.close_time IS NOT NULL
                            THEN COALESCE(o.party_size, 1) ELSE 0 END), 0) AS covers,
          COALESCE(SUM(CASE WHEN o.open_time IS NOT NULL AND o.close_time IS NOT NULL
                            THEN TIMESTAMPDIFF(MINUTE, o.open_time, o.close_time)
                            ELSE 0 END), 0) AS occupied_minutes,
          COALESCE(SUM(CASE WHEN o.open_time IS NOT NULL AND o.close_time IS NOT NULL
                            THEN TIMESTAMPDIFF(MINUTE, o.open_time, o.close_time)
                                 * COALESCE(o.party_size, 1)
                            ELSE 0 END), 0) AS occupied_seat_minutes
        FROM orders o
        WHERE o.status = 'DELIVERED'
          AND o.date >= :start
          AND o.date < :end
        """;

    Query aggregateQuery =
        entityManager
            .createNativeQuery(aggregateSql)
            .setParameter("start", startInclusive.atStartOfDay())
            .setParameter("end", endExclusive.atStartOfDay());
    Object[] aggregate = (Object[]) aggregateQuery.getSingleResult();

    long totalOrders = toLong(aggregate[0]);
    long ordersWithTimestamps = toLong(aggregate[1]);
    long covers = toLong(aggregate[2]);
    long occupiedMinutes = toLong(aggregate[3]);
    long occupiedSeatMinutes = toLong(aggregate[4]);

    String dayPartSql =
        """
        SELECT
          CASE
            WHEN TIME(o.open_time) >= :lunchStart AND TIME(o.open_time) < :lunchEnd THEN 'LUNCH'
            WHEN TIME(o.open_time) >= :dinnerStart AND TIME(o.open_time) < :dinnerEnd THEN 'DINNER'
            ELSE 'OTHER'
          END AS day_part,
          COALESCE(SUM(COALESCE(o.party_size, 1)), 0) AS covers
        FROM orders o
        WHERE o.status = 'DELIVERED'
          AND o.date >= :start
          AND o.date < :end
          AND o.open_time IS NOT NULL
          AND o.close_time IS NOT NULL
        GROUP BY day_part
        """;

    Query dayPartQuery =
        entityManager
            .createNativeQuery(dayPartSql)
            .setParameter("start", startInclusive.atStartOfDay())
            .setParameter("end", endExclusive.atStartOfDay())
            .setParameter("lunchStart", lunchStart.toString())
            .setParameter("lunchEnd", lunchEnd.toString())
            .setParameter("dinnerStart", dinnerStart.toString())
            .setParameter("dinnerEnd", dinnerEnd.toString());
    @SuppressWarnings("unchecked")
    List<Object[]> dayPartRows = dayPartQuery.getResultList();
    List<DayPartCovers> byDayPart = new ArrayList<>();
    for (Object[] row : dayPartRows) {
      byDayPart.add(new DayPartCovers((String) row[0], toLong(row[1])));
    }

    String tablesSql = "SELECT COUNT(*), COALESCE(SUM(capacity), 0) FROM tables";
    Object[] tables = (Object[]) entityManager.createNativeQuery(tablesSql).getSingleResult();
    long tableCount = toLong(tables[0]);
    long seatCount = toLong(tables[1]);

    return new OperationsSnapshot(
        totalOrders,
        ordersWithTimestamps,
        covers,
        occupiedMinutes,
        occupiedSeatMinutes,
        netSales,
        byDayPart,
        tableCount,
        seatCount);
  }

  /** {@inheritDoc} */
  @Override
  public Money loadLaborCost(LocalDate startInclusive, LocalDate endExclusive) {
    String sql =
        """
        SELECT COALESCE(SUM(labor_total), 0)
        FROM monthly_financial_summary
        WHERE bucket = 'daily'
          AND period_key >= :from
          AND period_key < :to
        """;
    Query query =
        entityManager
            .createNativeQuery(sql)
            .setParameter("from", startInclusive.toString())
            .setParameter("to", endExclusive.toString());
    BigDecimal labor = toBigDecimal(query.getSingleResult());
    return new Money(labor, currencyProvider.getCurrency());
  }

  private Money loadNetSales(LocalDate startInclusive, LocalDate endExclusive) {
    String sql =
        """
        SELECT COALESCE(SUM(od.unit_price), 0)
        FROM order_details od
        JOIN orders o ON o.id = od.order_id
        WHERE o.status = 'DELIVERED'
          AND o.date >= :start
          AND o.date < :end
        """;
    Query query =
        entityManager
            .createNativeQuery(sql)
            .setParameter("start", startInclusive.atStartOfDay())
            .setParameter("end", endExclusive.atStartOfDay());
    return new Money(toBigDecimal(query.getSingleResult()), currencyProvider.getCurrency());
  }

  private static long toLong(Object value) {
    return value == null ? 0L : ((Number) value).longValue();
  }

  private static BigDecimal toBigDecimal(Object value) {
    if (value == null) {
      return BigDecimal.ZERO;
    }
    return value instanceof BigDecimal decimal ? decimal : new BigDecimal(value.toString());
  }
}
