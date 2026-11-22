/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.port.output;

import aros.services.rms.core.systemconfig.domain.NotificationSettings;
import java.util.Optional;

/** Output port for the notification preferences singleton. */
public interface NotificationPreferencesPort {

  /**
   * Returns the stored notification preferences.
   *
   * @return the preferences when they have been persisted
   */
  Optional<NotificationSettings> find();

  /**
   * Persists the notification preferences.
   *
   * @param settings the preferences to save
   * @return the saved preferences
   */
  NotificationSettings save(NotificationSettings settings);
}
