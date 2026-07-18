/* (C) 2026 */

package aros.services.rms.core.analytics.domain.exception;

/** Thrown when an analytics configuration update violates its validation rules. */
public class InvalidAnalyticsConfigException extends RuntimeException {

  /**
   * Creates an exception with a validation failure message.
   *
   * @param message the validation failure message
   */
  public InvalidAnalyticsConfigException(String message) {
    super(message);
  }
}
