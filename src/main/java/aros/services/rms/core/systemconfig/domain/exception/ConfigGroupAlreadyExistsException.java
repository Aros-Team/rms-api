/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.exception;

/** Thrown when a configuration group name is already in use. */
public class ConfigGroupAlreadyExistsException extends SettingsException {

  /**
   * Creates the exception.
   *
   * @param name the duplicated group name
   */
  public ConfigGroupAlreadyExistsException(String name) {
    super(SettingsErrorType.CONFLICT, "Configuration group already exists: " + name);
  }
}
