package aros.services.rms.core.order.application.exception;

import aros.services.rms.core.order.domain.OrderStatus;

public class InvalidOrderStatusException extends RuntimeException {

  public InvalidOrderStatusException(OrderStatus currentStatus, String operation) {
    super("Cannot perform operation '" + operation + "' when order status is: " + currentStatus);
  }

  public InvalidOrderStatusException(OrderStatus currentStatus, OrderStatus expectedStatus) {
    super("Invalid order status: expected " + expectedStatus + " but was " + currentStatus);
  }
}
