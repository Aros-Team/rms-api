package aros.services.rms.infraestructure.common.exception;

import aros.services.rms.core.order.application.exception.InvalidOrderStatusException;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.application.exception.ProductNotFoundException;
import aros.services.rms.core.order.application.exception.TableNotAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  @ExceptionHandler(TableNotAvailableException.class)
  public ResponseEntity<ErrorResponse> handleTableNotAvailable(TableNotAvailableException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  @ExceptionHandler(InvalidOrderStatusException.class)
  public ResponseEntity<ErrorResponse> handleInvalidOrderStatus(InvalidOrderStatusException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }
}
