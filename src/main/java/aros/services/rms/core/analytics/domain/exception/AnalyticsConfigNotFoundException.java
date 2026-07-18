/* (C) 2026 */

package aros.services.rms.core.analytics.domain.exception;

/** Thrown when the analytics configuration singleton cannot be found. */
public class AnalyticsConfigNotFoundException extends RuntimeException {

  /** Creates an exception for the missing analytics configuration singleton. */
  public AnalyticsConfigNotFoundException() {
    super("Analytics config singleton not found");
  }
}
