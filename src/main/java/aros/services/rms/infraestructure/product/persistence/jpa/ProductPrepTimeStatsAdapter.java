/* (C) 2026 */

package aros.services.rms.infraestructure.product.persistence.jpa;

import aros.services.rms.core.product.domain.ProductPrepTimeStats;
import aros.services.rms.core.product.port.output.ProductPrepTimeStatsPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/** Reads measured preparation times from {@code order_details}. */
@Component
public class ProductPrepTimeStatsAdapter implements ProductPrepTimeStatsPort {

  private static final int MAX_VALID_MINUTES = 240;

  @PersistenceContext private EntityManager entityManager;

  /** {@inheritDoc} */
  @Override
  public ProductPrepTimeStats findByProductId(Long productId) {
    if (productId == null) {
      return ProductPrepTimeStats.empty();
    }
    String sql =
        """
        SELECT AVG(TIMESTAMPDIFF(SECOND, d.preparation_started_at, d.ready_at)) / 60.0 AS avg_minutes,
               COUNT(*) AS sample_count,
               MIN(d.preparation_started_at) AS first_sample,
               MAX(d.ready_at) AS last_sample
        FROM order_details d
        WHERE d.product_id = :productId
          AND d.preparation_started_at IS NOT NULL
          AND d.ready_at IS NOT NULL
          AND d.ready_at > d.preparation_started_at
          AND TIMESTAMPDIFF(MINUTE, d.preparation_started_at, d.ready_at) <= :maxMinutes
        """;
    Object[] row =
        (Object[])
            entityManager
                .createNativeQuery(sql)
                .setParameter("productId", productId)
                .setParameter("maxMinutes", MAX_VALID_MINUTES)
                .getSingleResult();
    if (row == null) {
      return ProductPrepTimeStats.empty();
    }
    long count = row[1] == null ? 0 : ((Number) row[1]).longValue();
    if (count == 0) {
      return ProductPrepTimeStats.empty();
    }
    Double average = row[0] == null ? null : new BigDecimal(row[0].toString()).doubleValue();
    return new ProductPrepTimeStats(
        average, count, toLocalDateTime(row[2]), toLocalDateTime(row[3]));
  }

  private static LocalDateTime toLocalDateTime(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalDateTime localDateTime) {
      return localDateTime;
    }
    if (value instanceof Timestamp timestamp) {
      return timestamp.toLocalDateTime();
    }
    return LocalDateTime.parse(value.toString().replace(' ', 'T'));
  }
}
