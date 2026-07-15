/* (C) 2026 */

package aros.services.rms.core.common.money.domain.exception;

/** Exception thrown when a Money amount has a scale greater than the maximum allowed (10). */
public class InvalidMoneyScaleException extends RuntimeException {

  private final int scale;

  /**
   * Creates a new exception for invalid money scale.
   *
   * @param scale the scale that exceeded the limit
   */
  public InvalidMoneyScaleException(int scale) {
    super("Invalid money scale: " + scale + " (max allowed: 10)");
    this.scale = scale;
  }

  /**
   * Returns the scale that exceeded the maximum allowed.
   *
   * @return the invalid scale
   */
  public int getScale() {
    return scale;
  }
}
