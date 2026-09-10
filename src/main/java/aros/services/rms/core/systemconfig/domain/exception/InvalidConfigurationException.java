/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.exception;

/** Thrown when a system configuration entry has an invalid key or value. */
public class InvalidConfigurationException extends RuntimeException {

  /**
   * Creates the exception.
   *
   * @param key the configuration key
   * @param value the invalid value
   */
  public InvalidConfigurationException(String key, String value) {
    super("Invalid configuration: key=" + key + ", value=" + value);
  }
}
