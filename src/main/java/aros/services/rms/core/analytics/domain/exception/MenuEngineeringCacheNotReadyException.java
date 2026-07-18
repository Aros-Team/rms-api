/* (C) 2026 */

package aros.services.rms.core.analytics.domain.exception;

/** Thrown when the menu engineering cache is empty for the requested period. Maps to HTTP 404. */
public class MenuEngineeringCacheNotReadyException extends RuntimeException {

  /**
   * Creates an exception with a descriptive message.
   *
   * @param bucket the time bucket
   * @param from the inclusive start period key
   * @param to the inclusive end period key
   */
  public MenuEngineeringCacheNotReadyException(String bucket, String from, String to) {
    super("Menu engineering cache not ready for bucket=" + bucket + " from=" + from + " to=" + to);
  }
}
