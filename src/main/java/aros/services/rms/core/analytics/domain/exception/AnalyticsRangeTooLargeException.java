/* (C) 2026 */

package aros.services.rms.core.analytics.domain.exception;

/** Raised when an analytics range exceeds the contract's maximum span. */
public class AnalyticsRangeTooLargeException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates the exception with a human-readable detail.
   *
   * @param message the detail message
   */
  public AnalyticsRangeTooLargeException(String message) {
    super(message);
  }
}
