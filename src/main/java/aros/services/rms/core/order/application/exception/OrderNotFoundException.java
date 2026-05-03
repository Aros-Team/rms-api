/* (C) 2026 */

package aros.services.rms.core.order.application.exception;

/** Exception thrown when an order is not found by its identifier. */
public class OrderNotFoundException extends RuntimeException {

  /**
   * Creates a new exception for missing order.
   *
   * @param id the order identifier that was not found
   */
  public OrderNotFoundException(Long id) {
    super("Order not found: " + id);
  }
}
