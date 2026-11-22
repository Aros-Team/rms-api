/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import aros.services.rms.core.systemconfig.domain.NotificationSettings;
import io.swagger.v3.oas.annotations.media.Schema;

/** Request payload for notification preferences. */
@Schema(description = "Global notification preferences")
public record NotificationSettingsRequest(
    @Schema(description = "Play a sound for new kitchen tickets", example = "true")
        boolean kitchenSoundEnabled,
    @Schema(description = "Vibrate the kitchen device for new tickets", example = "false")
        boolean kitchenHapticsEnabled,
    @Schema(description = "Raise low-stock alerts", example = "true") boolean lowStockAlertsEnabled,
    @Schema(description = "Raise order lifecycle alerts", example = "true")
        boolean orderAlertsEnabled,
    @Schema(description = "Send the daily summary", example = "false")
        boolean dailySummaryEnabled) {

  /**
   * Converts the request to the domain settings.
   *
   * @return the domain settings
   */
  public NotificationSettings toDomain() {
    return new NotificationSettings(
        kitchenSoundEnabled,
        kitchenHapticsEnabled,
        lowStockAlertsEnabled,
        orderAlertsEnabled,
        dailySummaryEnabled);
  }

  /**
   * Builds a response payload from the domain settings.
   *
   * @param settings the domain settings
   * @return the response payload
   */
  public static NotificationSettingsRequest fromDomain(NotificationSettings settings) {
    return new NotificationSettingsRequest(
        settings.kitchenSoundEnabled(),
        settings.kitchenHapticsEnabled(),
        settings.lowStockAlertsEnabled(),
        settings.orderAlertsEnabled(),
        settings.dailySummaryEnabled());
  }
}
