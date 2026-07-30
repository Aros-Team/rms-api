/* (C) 2026 */

package aros.services.rms.core.analytics.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.time.Instant;
import java.util.List;

/**
 * Domain report for menu engineering BCG analysis — mirrors the §5.2 contract shape.
 *
 * <p>Contains the period info, median values, per-item BCG classification, cache status, and data
 * completeness flags.
 */
public record MenuEngineeringReport(
    PeriodInfo period,
    MedianInfo median,
    List<MenuItemSummary> items,
    CacheStatus cacheStatus,
    String dataCompleteness,
    List<String> notes) {

  /** Describes the queried time range. */
  public record PeriodInfo(String bucket, String from, String to, List<String> keys) {}

  /** Median volume and margin across all items. */
  public record MedianInfo(int volume, Money margin) {}

  /** One menu item with its BCG quadrant assignment. */
  public record MenuItemSummary(
      Long productId,
      String productName,
      Long categoryId,
      String categoryName,
      int unitsSold,
      Money revenue,
      Money recipeCost,
      Money avgOptionCost,
      Money effectiveCost,
      Money grossProfitPerUnit,
      Money totalContribution,
      BcgQuadrant quadrant) {}

  /** Cache validity information. */
  public record CacheStatus(Instant lastRefreshedAt, String sourceVersion, long ttlSeconds) {}
}
