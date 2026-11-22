/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.input;

import aros.services.rms.core.systemconfig.domain.NotificationSettings;

/** Use case for reading notification preferences. */
public interface GetNotificationSettingsUseCase {

  /**
   * Returns the stored notification preferences.
   *
   * @return the notification settings
   */
  NotificationSettings get();
}
