/* (C) 2026 */

package aros.services.rms.core.order.application.exception;

import aros.services.rms.core.order.domain.OrderStatus;

/** Exception thrown when an operation is attempted on an order with an invalid status. */
public class InvalidOrderStatusException extends RuntimeException {

  /**
   * Creates a new exception for operation on invalid status.
   *
   * @param currentStatus the current order status
   * @param operation the operation that was attempted
   */
  public InvalidOrderStatusException(OrderStatus currentStatus, String operation) {
    super("Cannot perform operation '" + operation + "' when order status is: " + currentStatus);
  }

  /**
   * Creates a new exception for status mismatch.
   *
   * @param currentStatus the current order status
   * @param expectedStatus the expected order status
   */
  public InvalidOrderStatusException(OrderStatus currentStatus, OrderStatus expectedStatus) {
    super("Invalid order status: expected " + expectedStatus + " but was " + currentStatus);
  }
}
