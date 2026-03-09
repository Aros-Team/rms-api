/* (C) 2026 */
package aros.services.rms.infraestructure.common.exception;

import aros.services.rms.core.area.application.exception.AreaAlreadyExistsException;
import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;
import aros.services.rms.core.category.application.exception.CategoryNotFoundException;
import aros.services.rms.core.category.application.exception.OptionCategoryNotFoundException;
import aros.services.rms.core.order.application.exception.InvalidOrderStatusException;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.application.exception.TableNotAvailableException;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.product.application.exception.ProductOptionNotFoundException;
import aros.services.rms.core.table.application.exception.InvalidTableStatusException;
import aros.services.rms.core.table.application.exception.TableNotFoundException;
import aros.services.rms.infraestructure.auth.exception.JwtKeysMissingException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Global exception handler that maps core exceptions to HTTP responses. */
@RestControllerAdvice
@Order(10)
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidCredentials(
      InvalidCredentialsException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            Map.of(
                "error", "Invalid credentials",
                "message", "Usuario o contraseña incorrectos"));
  }

  @ExceptionHandler(JwtKeysMissingException.class)
  public ResponseEntity<ErrorResponse> handleJwtKeysMissing(JwtKeysMissingException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(500, e.getMessage()));
  }

  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleServiceUnavailable(ServiceUnavailableException e) {
    log.warn("Servicio no disponible: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(new ErrorResponse(503, e.getMessage()));
  }

  // --- Authorization handlers ---

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AuthorizationDeniedException e) {
    log.warn("accesso denegado: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
  }

  // --- Generic catch-all handlers ---

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
    log.error(
        "Error interno no esperado: mensaje={}, tipo={}",
        e.getMessage(),
        e.getClass().getName(),
        e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(500, "Error interno del servidor"));
  }

  @ExceptionHandler(Throwable.class)
  public ResponseEntity<ErrorResponse> handleThrowable(Throwable t) {
    log.error(
        "Error crítico del sistema: mensaje={}, tipo={}",
        t.getMessage(),
        t.getClass().getName(),
        t);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(500, "Error interno del servidor"));
  }
}
