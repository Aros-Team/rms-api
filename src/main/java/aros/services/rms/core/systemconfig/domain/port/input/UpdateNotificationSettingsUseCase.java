/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.input;

import aros.services.rms.core.systemconfig.domain.NotificationSettings;

/** Use case for updating notification preferences. */
public interface UpdateNotificationSettingsUseCase {

  /**
   * Persists the notification preferences.
   *
   * @param settings the new preferences
   * @return the persisted preferences
   */
  NotificationSettings update(NotificationSettings settings);
}
