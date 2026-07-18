/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.repository;

import aros.services.rms.core.analytics.infrastructure.persistence.entity.MenuPerformanceCacheEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data repository for the menu_performance_cache entity. */
public interface JpaMenuPerformanceCacheRepository
    extends JpaRepository<MenuPerformanceCacheEntity, Long> {

  /**
   * Finds a cache row by its unique product + period + bucket.
   *
   * @param productId the product ID
   * @param periodKey the period key
   * @param bucket the time bucket
   * @return the matching entity, if found
   */
  Optional<MenuPerformanceCacheEntity> findByProductIdAndPeriodKeyAndBucket(
      Long productId, String periodKey, String bucket);

  /**
   * Finds all cache rows for a given bucket and period key range, with optional category filter.
   *
   * @param bucket the time bucket
   * @param fromKey the inclusive start
   * @param toKey the inclusive end
   * @return the matching entities
   */
  @Query(
      "SELECT m FROM MenuPerformanceCacheEntity m WHERE m.bucket = :bucket "
          + "AND m.periodKey BETWEEN :fromKey AND :toKey ORDER BY m.periodKey ASC, m.productId ASC")
  List<MenuPerformanceCacheEntity> findByBucketAndPeriodKeyBetween(
      @Param("bucket") String bucket,
      @Param("fromKey") String fromKey,
      @Param("toKey") String toKey);

  /**
   * Finds all cache rows for a given bucket, period key range and category.
   *
   * @param bucket the time bucket
   * @param fromKey the inclusive start
   * @param toKey the inclusive end
   * @param categoryId the category ID
   * @return the matching entities
   */
  @Query(
      "SELECT m FROM MenuPerformanceCacheEntity m WHERE m.bucket = :bucket "
          + "AND m.periodKey BETWEEN :fromKey AND :toKey AND m.categoryId = :categoryId "
          + "ORDER BY m.periodKey ASC, m.productId ASC")
  List<MenuPerformanceCacheEntity> findByBucketAndPeriodKeyBetweenAndCategory(
      @Param("bucket") String bucket,
      @Param("fromKey") String fromKey,
      @Param("toKey") String toKey,
      @Param("categoryId") Long categoryId);

  /**
   * Deletes all cache rows for a given product.
   *
   * @param productId the product ID
   */
  @Modifying
  @Query("DELETE FROM MenuPerformanceCacheEntity m WHERE m.productId = :productId")
  void deleteByProductId(@Param("productId") Long productId);

  /**
   * Finds the maximum source_version from the cache.
   *
   * @return the latest source version, or null if cache is empty
   */
  @Query("SELECT MAX(m.sourceVersion) FROM MenuPerformanceCacheEntity m")
  String findLatestSourceVersion();
}
