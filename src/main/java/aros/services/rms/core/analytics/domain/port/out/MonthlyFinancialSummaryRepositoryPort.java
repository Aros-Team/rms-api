/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.out;

import aros.services.rms.core.analytics.domain.MonthlyFinancialSummary;
import java.util.List;
import java.util.Optional;

/** Output port for monthly financial summary persistence. */
public interface MonthlyFinancialSummaryRepositoryPort {

  /**
   * Finds a summary row by its period key and bucket.
   *
   * @param periodKey the period key (e.g. "2026-07")
   * @param bucket the time bucket (daily, weekly, monthly, yearly)
   * @return the matching summary, if found
   */
  Optional<MonthlyFinancialSummary> findByPeriodKeyAndBucket(String periodKey, String bucket);

  /**
   * Upserts a summary row. Uses REPLACE INTO semantics (period_key + bucket unique constraint).
   *
   * @param summary the summary to upsert
   */
  void upsert(MonthlyFinancialSummary summary);

  /**
   * Finds all summary rows for a given bucket and period key range, ordered by period_key ASC.
   *
   * @param bucket the time bucket
   * @param fromKey the inclusive start period key
   * @param toKey the inclusive end period key
   * @return the matching summaries
   */
  List<MonthlyFinancialSummary> findByBucketAndPeriodKeyBetween(
      String bucket, String fromKey, String toKey);
}
