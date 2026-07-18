/* (C) 2026 */

package aros.services.rms.core.analytics.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Singleton configuration for analytics operating hours and alert thresholds. */
public record AnalyticsConfig(
    Integer id,
    LocalTime defaultOpen,
    LocalTime defaultClose,
    LocalTime lunchStart,
    LocalTime lunchEnd,
    LocalTime dinnerStart,
    LocalTime dinnerEnd,
    BigDecimal foodCostDeviationPp,
    BigDecimal laborCostDeviationPp,
    BigDecimal salesDropYoyPct,
    LocalDateTime updatedAt,
    Long updatedBy) {

  /**
   * Creates analytics configuration after validating singleton, time-window, and threshold
   * invariants.
   *
   * @param id the singleton identifier, which must be 1
   * @param defaultOpen the default opening time
   * @param defaultClose the default closing time
   * @param lunchStart the lunch period start time
   * @param lunchEnd the lunch period end time
   * @param dinnerStart the dinner period start time
   * @param dinnerEnd the dinner period end time
   * @param foodCostDeviationPp the non-negative food cost deviation threshold
   * @param laborCostDeviationPp the non-negative labor cost deviation threshold
   * @param salesDropYoyPct the non-negative year-over-year sales drop threshold
   * @param updatedAt the last update timestamp
   * @param updatedBy the identifier of the user who last updated the config, or null
   * @throws IllegalArgumentException if any invariant is violated
   */
  public AnalyticsConfig {
    if (id == null || id != 1) {
      throw new IllegalArgumentException("AnalyticsConfig.id must be 1 (singleton)");
    }
    if (defaultOpen == null || defaultClose == null || !defaultOpen.isBefore(defaultClose)) {
      throw new IllegalArgumentException("defaultOpen must be strictly before defaultClose");
    }
    if (lunchStart == null || lunchEnd == null || !lunchStart.isBefore(lunchEnd)) {
      throw new IllegalArgumentException("lunchStart must be strictly before lunchEnd");
    }
    if (dinnerStart == null || dinnerEnd == null || !dinnerStart.isBefore(dinnerEnd)) {
      throw new IllegalArgumentException("dinnerStart must be strictly before dinnerEnd");
    }
    if (foodCostDeviationPp == null || foodCostDeviationPp.signum() < 0) {
      throw new IllegalArgumentException("foodCostDeviationPp must be >= 0");
    }
    if (laborCostDeviationPp == null || laborCostDeviationPp.signum() < 0) {
      throw new IllegalArgumentException("laborCostDeviationPp must be >= 0");
    }
    if (salesDropYoyPct == null || salesDropYoyPct.signum() < 0) {
      throw new IllegalArgumentException("salesDropYoyPct must be >= 0");
    }
    if (updatedAt == null) {
      throw new IllegalArgumentException("updatedAt must not be null");
    }
  }

  /**
   * Creates seeded analytics defaults at the supplied update time.
   *
   * @param now the update timestamp for the seeded config
   * @return singleton analytics configuration with seeded defaults
   */
  public static AnalyticsConfig defaults(LocalDateTime now) {
    return new AnalyticsConfig(
        1,
        LocalTime.of(11, 0),
        LocalTime.of(23, 0),
        LocalTime.of(11, 0),
        LocalTime.of(15, 0),
        LocalTime.of(18, 0),
        LocalTime.of(23, 0),
        new BigDecimal("2.00"),
        new BigDecimal("3.00"),
        new BigDecimal("10.00"),
        now,
        null);
  }
}
