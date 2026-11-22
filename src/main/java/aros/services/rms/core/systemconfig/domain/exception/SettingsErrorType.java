/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.exception;

/** Category used by the API layer to map a settings failure to an HTTP status. */
public enum SettingsErrorType {
  /** The request is syntactically valid but its values are not acceptable. */
  INVALID,
  /** The referenced settings resource does not exist. */
  NOT_FOUND,
  /** The change conflicts with the current state. */
  CONFLICT
}
