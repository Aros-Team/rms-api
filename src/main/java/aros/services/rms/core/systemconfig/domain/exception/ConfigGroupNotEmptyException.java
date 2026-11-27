/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.exception;

/** Thrown when a configuration group still contains entries. */
public class ConfigGroupNotEmptyException extends SettingsException {

  /**
   * Creates the exception.
   *
   * @param id the group id
   * @param entries the number of entries still assigned to the group
   */
  public ConfigGroupNotEmptyException(Long id, int entries) {
    super(
        SettingsErrorType.CONFLICT,
        "El grupo de configuración " + id + " todavía tiene " + entries + " entradas");
  }
}
