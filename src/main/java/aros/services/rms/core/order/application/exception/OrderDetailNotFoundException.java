/* (C) 2026 */

package aros.services.rms.core.order.application.exception;

/** Exception thrown when an order detail (line) is not found inside an order. */
public class OrderDetailNotFoundException extends RuntimeException {

  /**
   * Creates a new exception for a missing order detail.
   *
   * @param orderId the order identifier
   * @param detailId the order detail identifier that was not found
   */
  public OrderDetailNotFoundException(Long orderId, Long detailId) {
    super("Order detail not found: " + detailId + " in order " + orderId);
  }
}
