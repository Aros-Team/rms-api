/* (C) 2026 */

package aros.services.rms.core.analytics.domain.exception;

/** Thrown when no prime cost data exists for the requested period range. Maps to HTTP 404. */
public class PrimeCostPeriodNotFoundException extends RuntimeException {

  /**
   * Creates an exception with a descriptive message including the request parameters.
   *
   * @param bucket the time bucket that was requested
   * @param from the inclusive start period key
   * @param to the inclusive end period key
   */
  public PrimeCostPeriodNotFoundException(String bucket, String from, String to) {
    super("No prime-cost data found for bucket=" + bucket + " from=" + from + " to=" + to);
  }
}
