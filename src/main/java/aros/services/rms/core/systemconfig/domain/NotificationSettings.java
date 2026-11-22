/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain;

/**
 * Global notification preferences consumed by the kitchen and admin surfaces.
 *
 * @param kitchenSoundEnabled whether new kitchen tickets play a sound
 * @param kitchenHapticsEnabled whether the kitchen device vibrates on new tickets
 * @param lowStockAlertsEnabled whether low-stock alerts are raised
 * @param orderAlertsEnabled whether order lifecycle alerts are raised
 * @param dailySummaryEnabled whether the daily summary notification is enabled
 */
public record NotificationSettings(
    boolean kitchenSoundEnabled,
    boolean kitchenHapticsEnabled,
    boolean lowStockAlertsEnabled,
    boolean orderAlertsEnabled,
    boolean dailySummaryEnabled) {

  /**
   * Returns the documented defaults.
   *
   * @return sound and stock alerts enabled, haptics and summaries disabled
   */
  public static NotificationSettings defaults() {
    return new NotificationSettings(true, false, true, true, false);
  }
}
