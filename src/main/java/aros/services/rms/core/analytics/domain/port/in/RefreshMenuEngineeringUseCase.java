/* (C) 2026 */

package aros.services.rms.core.analytics.domain.port.in;

/** Input port for refreshing (recomputing) the menu engineering BCG cache for a period. */
public interface RefreshMenuEngineeringUseCase {

  /**
   * Recomputes the BCG quadrant analysis for all active products in the given period and upserts
   * results into the menu_performance_cache table.
   *
   * @param bucket the time bucket (daily, weekly, monthly, yearly)
   * @param periodKey the period key (e.g. "2026-07" for monthly)
   */
  void refresh(String bucket, String periodKey);
}
