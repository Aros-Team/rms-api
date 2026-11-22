/* (C) 2026 */

package aros.services.rms.core.payment.application.exception;

/** Thrown when trying to void a payment that is already voided. Maps to HTTP 409. */
public class PaymentAlreadyVoidedException extends RuntimeException {

  /**
   * Creates the exception.
   *
   * @param paymentId the already voided payment
   */
  public PaymentAlreadyVoidedException(Long paymentId) {
    super("Payment " + paymentId + " is already voided");
  }
}
