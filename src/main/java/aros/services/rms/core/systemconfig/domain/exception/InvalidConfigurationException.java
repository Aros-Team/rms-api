/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.exception;

/** Thrown when a system configuration entry has an invalid key or value. */
public class InvalidConfigurationException extends SettingsException {

  /**
   * Creates the exception.
   *
   * @param key the configuration key
   * @param value the invalid value
   */
  public InvalidConfigurationException(String key, String value) {
    super(SettingsErrorType.INVALID, "Invalid configuration: key=" + key + ", value=" + value);
  }

  /**
   * Creates the exception with a free-form message.
   *
   * @param message the human-readable detail
   */
  public InvalidConfigurationException(String message) {
    super(SettingsErrorType.INVALID, message);
  }
}
