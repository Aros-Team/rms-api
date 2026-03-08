/* (C) 2026 */
package aros.services.rms.infraestructure.common.exception;

import aros.services.rms.core.area.application.exception.AreaAlreadyExistsException;
import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.category.application.exception.CategoryNotFoundException;
import aros.services.rms.core.category.application.exception.OptionCategoryNotFoundException;
import aros.services.rms.core.order.application.exception.InvalidOrderStatusException;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.application.exception.TableNotAvailableException;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.product.application.exception.ProductOptionNotFoundException;
import aros.services.rms.core.table.application.exception.InvalidTableStatusException;
import aros.services.rms.core.table.application.exception.TableNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Global exception handler that maps core exceptions to HTTP responses. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  // --- Order exceptions ---

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  @ExceptionHandler(
      aros.services.rms.core.order.application.exception.ProductNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderProductNotFound(
      aros.services.rms.core.order.application.exception.ProductNotFoundException e) {
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

  // --- Area exceptions ---

  @ExceptionHandler(AreaNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAreaNotFound(AreaNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  @ExceptionHandler(AreaAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleAreaAlreadyExists(AreaAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  // --- Category exceptions ---

  @ExceptionHandler(CategoryNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCategoryNotFound(CategoryNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  @ExceptionHandler(OptionCategoryNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOptionCategoryNotFound(
      OptionCategoryNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  // --- Product exceptions ---

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  @ExceptionHandler(ProductOptionNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleProductOptionNotFound(
      ProductOptionNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  // --- Table exceptions ---

  @ExceptionHandler(TableNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleTableNotFound(TableNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  @ExceptionHandler(InvalidTableStatusException.class)
  public ResponseEntity<ErrorResponse> handleInvalidTableStatus(InvalidTableStatusException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  // --- Validation exceptions ---

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation failed");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, message));
  }

  // --- Generic exceptions ---

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage()));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }
}
