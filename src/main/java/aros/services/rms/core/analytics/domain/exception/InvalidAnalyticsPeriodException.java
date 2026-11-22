/* (C) 2026 */

package aros.services.rms.core.analytics.domain.exception;

/** Raised when a period key or bucket does not match the analytics contract. */
public class InvalidAnalyticsPeriodException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception with a human-readable detail.
   *
   * @param message the detail message
   */
  public InvalidAnalyticsPeriodException(String message) {
    super(message);
  }
}
