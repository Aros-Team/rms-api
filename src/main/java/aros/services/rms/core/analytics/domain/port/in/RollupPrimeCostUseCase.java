/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.in;

import java.time.LocalDate;

/** Input port for the daily-to-monthly prime cost rollup. */
public interface RollupPrimeCostUseCase {

  /**
   * Rolls the daily summaries between two dates into their weekly, monthly and yearly buckets.
   *
   * <p>The operation is idempotent: rows are upserted on {@code (period_key, bucket)} and running
   * it twice produces the same values.
   *
   * @param from inclusive first day
   * @param to inclusive last day
   * @return the number of bucket rows written or updated
   */
  int rollup(LocalDate from, LocalDate to);
}
