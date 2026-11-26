/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.exception;

/** Thrown when a configuration group does not exist. */
public class ConfigGroupNotFoundException extends SettingsException {

  /**
   * Creates the exception.
   *
   * @param id the group id or name that was requested
   */
  public ConfigGroupNotFoundException(String id) {
    super(SettingsErrorType.NOT_FOUND, "Grupo de configuración no encontrado: " + id);
  }
}
