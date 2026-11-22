/* (C) 2026 */

package aros.services.rms.core.analytics.domain.exception;

/** Raised when an analytics range is empty because {@code to} is before {@code from}. */
public class InvalidAnalyticsRangeException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception with a human-readable detail.
   *
   * @param message the detail message
   */
  public InvalidAnalyticsRangeException(String message) {
    super(message);
  }
}
