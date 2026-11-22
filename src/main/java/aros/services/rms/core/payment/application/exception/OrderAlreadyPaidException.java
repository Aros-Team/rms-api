/* (C) 2026 */

package aros.services.rms.core.payment.application.exception;

/** Thrown when an order already has a non-voided payment. Maps to HTTP 409. */
public class OrderAlreadyPaidException extends RuntimeException {

  private final Long paymentId;

  /**
   * Creates the exception.
   *
   * @param orderId the already paid order
   * @param paymentId the active payment identifier
   */
  public OrderAlreadyPaidException(Long orderId, Long paymentId) {
    super("Order " + orderId + " is already paid by payment " + paymentId);
    this.paymentId = paymentId;
  }

  /**
   * Returns the active payment that blocks the new charge.
   *
   * @return the payment identifier
   */
  public Long getPaymentId() {
    return paymentId;
  }
}
