/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.out;

import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MenuItemSummary;
import java.util.List;

/** Output port for the menu_performance_cache persistence. */
public interface MenuEngineeringCacheRepositoryPort {

  /**
   * Finds cached menu items for a given bucket and period key range, optionally filtered by
   * category.
   *
   * @param bucket the time bucket
   * @param fromKey the inclusive start period key
   * @param toKey the inclusive end period key
   * @param categoryId optional category filter (null for all)
   * @return the matching cached items
   */
  List<MenuItemSummary> findByBucketAndPeriodKeyBetween(
      String bucket, String fromKey, String toKey, Long categoryId);

  /**
   * Upserts a menu item summary row into the cache.
   *
   * @param item the menu item summary to persist
   * @param bucket the time bucket
   * @param periodKey the period key
   * @param sourceVersion the version of the source data
   */
  void upsert(MenuItemSummary item, String bucket, String periodKey, String sourceVersion);

  /**
   * Deletes all cache rows for a given product (used on price/recipe change invalidation).
   *
   * @param productId the product ID
   */
  void deleteByProductId(Long productId);

  /**
   * Returns the most recent refresh source version from the cache, or null if empty.
   *
   * @return the source version string, or null
   */
  String findLatestSourceVersion();
}
