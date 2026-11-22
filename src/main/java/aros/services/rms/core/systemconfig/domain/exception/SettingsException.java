/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.exception;

/** Base exception for settings and configuration failures. */
public abstract class SettingsException extends RuntimeException {

  private final SettingsErrorType errorType;

  /**
   * Creates the exception.
   *
   * @param errorType the failure category
   * @param message the human-readable detail
   */
  protected SettingsException(SettingsErrorType errorType, String message) {
    super(message);
    this.errorType = errorType;
  }

  /**
   * Returns the failure category.
   *
   * @return the category
   */
  public SettingsErrorType getErrorType() {
    return errorType;
  }
}
