/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import aros.services.rms.core.systemconfig.domain.NotificationSettings;
import aros.services.rms.core.systemconfig.domain.exception.InvalidConfigurationException;
import aros.services.rms.core.systemconfig.domain.port.input.GetNotificationSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateNotificationSettingsUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.NotificationPreferencesPort;
import lombok.RequiredArgsConstructor;

/** Reads and updates the global notification preferences. */
@RequiredArgsConstructor
public class NotificationSettingsService
    implements GetNotificationSettingsUseCase, UpdateNotificationSettingsUseCase {

  private final NotificationPreferencesPort notificationPreferencesPort;

  /** {@inheritDoc} */
  @Override
  public NotificationSettings get() {
    return notificationPreferencesPort.find().orElseGet(NotificationSettings::defaults);
  }

  /** {@inheritDoc} */
  @Override
  public NotificationSettings update(NotificationSettings settings) {
    if (settings == null) {
      throw new InvalidConfigurationException("Notification settings are required");
    }
    return notificationPreferencesPort.save(settings);
  }
}
