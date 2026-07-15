/* (C) 2026 */

package aros.services.rms.core.common.money.domain.exception;

/** Exception thrown when a negative money value is not allowed. */
public class NegativeMoneyException extends RuntimeException {

  /**
   * Creates a new exception for negative money.
   *
   * @param message the detail message
   */
  public NegativeMoneyException(String message) {
    super(message);
  }
}
