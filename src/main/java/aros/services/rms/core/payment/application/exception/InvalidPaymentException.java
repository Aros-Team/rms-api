/* (C) 2026 */

package aros.services.rms.core.payment.application.exception;

/** Thrown when a payment request violates a business invariant. Maps to HTTP 400. */
public class InvalidPaymentException extends RuntimeException {

  /**
   * Creates the exception.
   *
   * @param message the human-readable reason
   */
  public InvalidPaymentException(String message) {
    super(message);
  }
}
