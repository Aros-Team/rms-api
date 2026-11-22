/* (C) 2026 */

package aros.services.rms.core.payment.application.exception;

/** Thrown when an order is not in a payable state. Maps to HTTP 409. */
public class OrderNotPayableException extends RuntimeException {

  /**
   * Creates the exception.
   *
   * @param orderId the order identifier
   * @param status the current order status
   */
  public OrderNotPayableException(Long orderId, String status) {
    super("Order " + orderId + " cannot be charged while it is " + status);
  }
}
