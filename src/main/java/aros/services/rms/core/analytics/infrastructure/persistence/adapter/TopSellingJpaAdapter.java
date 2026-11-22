/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.adapter;

import aros.services.rms.core.analytics.domain.port.out.TopSellingQueryPort;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Native-SQL adapter that ranks products by units sold. */
@Component
@RequiredArgsConstructor
public class TopSellingJpaAdapter implements TopSellingQueryPort {

  private final EntityManager entityManager;
  private final CurrencyProvider currencyProvider;

  /** {@inheritDoc} */
  @Override
  public List<ProductSales> findTopSelling(
      LocalDate startInclusive, LocalDate endExclusive, int limit) {
    String sql =
        """
        SELECT od.product_id,
               p.name,
               COUNT(*) AS units_sold,
               COALESCE(SUM(od.unit_price), 0) AS revenue
        FROM order_details od
        JOIN orders o ON o.id = od.order_id
        JOIN products p ON p.id = od.product_id
        WHERE o.status <> 'CANCELLED'
          AND o.date >= :start
          AND o.date < :end
        GROUP BY od.product_id, p.name
        ORDER BY units_sold DESC, revenue DESC, od.product_id ASC
        LIMIT :limit
        """;
    Query query =
        entityManager
            .createNativeQuery(sql)
            .setParameter("start", startInclusive.atStartOfDay())
            .setParameter("end", endExclusive.atStartOfDay())
            .setParameter("limit", limit);
    @SuppressWarnings("unchecked")
    List<Object[]> rows = query.getResultList();

    List<ProductSales> result = new ArrayList<>(rows.size());
    for (Object[] row : rows) {
      BigDecimal revenue = row[3] == null ? BigDecimal.ZERO : new BigDecimal(row[3].toString());
      result.add(
          new ProductSales(
              ((Number) row[0]).longValue(),
              (String) row[1],
              ((Number) row[2]).longValue(),
              new Money(revenue, currencyProvider.getCurrency())));
    }
    return result;
  }
}
