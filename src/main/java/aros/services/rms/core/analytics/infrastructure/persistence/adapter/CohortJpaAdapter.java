/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.adapter;

import aros.services.rms.core.analytics.domain.port.out.CohortQueryPort;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Native-SQL adapter for cohort visits.
 *
 * <p>The fingerprint is the stored phone hash, or a deterministic SHA-256 of the normalized phone
 * when only the phone is captured. Orders without a phone fall back to {@code table:<id>} and are
 * flagged as a table-only fingerprint.
 */
@Component
@RequiredArgsConstructor
public class CohortJpaAdapter implements CohortQueryPort {

  private final EntityManager entityManager;
  private final CurrencyProvider currencyProvider;

  /** {@inheritDoc} */
  @Override
  public List<CohortOrderRow> findOrders(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
    String sql =
        """
        SELECT
          o.id,
          o.date,
          COALESCE(
            o.customer_fingerprint,
            CASE WHEN o.customer_phone IS NOT NULL AND o.customer_phone <> ''
                 THEN SHA2(o.customer_phone, 256)
                 ELSE CONCAT('table:', COALESCE(o.table_id, 0))
            END
          ) AS fingerprint,
          CASE WHEN (o.customer_phone IS NOT NULL AND o.customer_phone <> '')
                    OR o.customer_fingerprint IS NOT NULL
               THEN 1 ELSE 0 END AS has_phone,
          COALESCE(o.party_size, 1) AS covers,
          COALESCE((SELECT SUM(od.unit_price) FROM order_details od WHERE od.order_id = o.id), 0)
            AS total
        FROM orders o
        WHERE o.status <> 'CANCELLED'
          AND o.date >= :from
          AND o.date < :to
        ORDER BY o.date ASC, o.id ASC
        """;
    Query query =
        entityManager
            .createNativeQuery(sql)
            .setParameter("from", fromInclusive)
            .setParameter("to", toExclusive);
    @SuppressWarnings("unchecked")
    List<Object[]> rows = query.getResultList();

    List<CohortOrderRow> result = new ArrayList<>(rows.size());
    for (Object[] row : rows) {
      result.add(
          new CohortOrderRow(
              ((Number) row[0]).longValue(),
              ((java.sql.Timestamp) row[1]).toLocalDateTime(),
              (String) row[2],
              ((Number) row[3]).intValue() == 1,
              ((Number) row[4]).longValue(),
              new Money(toBigDecimal(row[5]), currencyProvider.getCurrency())));
    }
    return result;
  }

  private static BigDecimal toBigDecimal(Object value) {
    if (value == null) {
      return BigDecimal.ZERO;
    }
    return value instanceof BigDecimal decimal ? decimal : new BigDecimal(value.toString());
  }
}
