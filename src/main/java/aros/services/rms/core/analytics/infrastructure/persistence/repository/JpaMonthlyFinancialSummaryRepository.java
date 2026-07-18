/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.repository;

import aros.services.rms.core.analytics.infrastructure.persistence.entity.MonthlyFinancialSummaryEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data repository for the monthly_financial_summary entity. */
public interface JpaMonthlyFinancialSummaryRepository
    extends JpaRepository<MonthlyFinancialSummaryEntity, Long> {

  /**
   * Finds a summary row by its unique period key + bucket.
   *
   * @param periodKey the period key
   * @param bucket the time bucket
   * @return the matching entity, if found
   */
  Optional<MonthlyFinancialSummaryEntity> findByPeriodKeyAndBucket(String periodKey, String bucket);

  /**
   * Finds all summary rows for a given bucket and period key range, ordered by period_key ASC.
   *
   * @param bucket the time bucket
   * @param fromKey the inclusive start
   * @param toKey the inclusive end
   * @return the matching entities
   */
  @Query(
      "SELECT m FROM MonthlyFinancialSummaryEntity m WHERE m.bucket = :bucket "
          + "AND m.periodKey BETWEEN :fromKey AND :toKey ORDER BY m.periodKey ASC")
  List<MonthlyFinancialSummaryEntity> findByBucketAndPeriodKeyBetween(
      @Param("bucket") String bucket,
      @Param("fromKey") String fromKey,
      @Param("toKey") String toKey);
}
