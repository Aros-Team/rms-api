/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.exception;

/** Thrown when a configuration entry does not exist. */
public class ConfigEntryNotFoundException extends SettingsException {

  /**
   * Creates the exception.
   *
   * @param key the requested configuration key
   */
  public ConfigEntryNotFoundException(String key) {
    super(SettingsErrorType.NOT_FOUND, "Configuration entry not found: " + key);
  }
}
