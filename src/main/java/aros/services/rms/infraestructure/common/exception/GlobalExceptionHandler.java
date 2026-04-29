/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.application.exception.StorageLocationNotFoundException;
import aros.services.rms.core.inventory.application.exception.SupplyAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyCategoryAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyVariantAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyVariantNotFoundException;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.application.exception.TableNotAvailableException;
import aros.services.rms.core.product.application.exception.InvalidProductOptionException;
import aros.services.rms.core.table.application.exception.InvalidTableStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Global exception handler that maps core exceptions to HTTP responses. */
@RestControllerAdvice
@Order(10)
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // --- Order exceptions ---

  /** Handles OrderNotFoundException. */
  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  /** Handles ProductNotFoundException in order context. */
  @ExceptionHandler(
      aros.services.rms.core.order.application.exception.ProductNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderProductNotFound(
      aros.services.rms.core.order.application.exception.ProductNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  /**
   * Handles TableNotAvailableException.
   *
   * @param e the exception
   * @return error response
   */
  @ExceptionHandler(TableNotAvailableException.class)
  public ResponseEntity<ErrorResponse> handleTableNotAvailable(TableNotAvailableException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  /** Handles InvalidTableStatusException. */
  @ExceptionHandler(InvalidTableStatusException.class)
  public ResponseEntity<ErrorResponse> handleInvalidTableStatus(InvalidTableStatusException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  // --- Inventory exceptions ---

  /** Handles InsufficientStockException. */
  @ExceptionHandler(InsufficientStockException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  /**
   * Handles SupplyVariantNotFoundException.
   *
   * @param e the exception
   * @return error response
   */
  @ExceptionHandler(SupplyVariantNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleSupplyVariantNotFound(
      SupplyVariantNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  /** Handles SupplyVariantAlreadyExistsException. */
  @ExceptionHandler(SupplyVariantAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleSupplyVariantAlreadyExists(
      SupplyVariantAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  /** Handles SupplyAlreadyExistsException. */
  @ExceptionHandler(SupplyAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleSupplyAlreadyExists(SupplyAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  /** Handles SupplyCategoryAlreadyExistsException. */
  @ExceptionHandler(SupplyCategoryAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleSupplyCategoryAlreadyExists(
      SupplyCategoryAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  /** Handles AccountSetupTokenAlreadyUsedException. */
  @ExceptionHandler(
      aros.services.rms.core.auth.application.exception.AccountSetupTokenAlreadyUsedException.class)
  public ResponseEntity<ErrorResponse> handleAccountSetupTokenAlreadyUsed(
      aros.services.rms.core.auth.application.exception.AccountSetupTokenAlreadyUsedException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  // --- Authorization handlers ---

  /** Handles AuthorizationDeniedException. */
  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AuthorizationDeniedException e) {
    log.warn("accesso denegado: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
  }

  // --- User exceptions ---

  /** Handles InvalidPasswordException. */
  @ExceptionHandler(
      aros.services.rms.core.user.application.exception.InvalidPasswordException.class)
  public ResponseEntity<ErrorResponse> handleInvalidPassword(
      aros.services.rms.core.user.application.exception.InvalidPasswordException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage()));
  }

  /** Handles SamePasswordException. */
  @ExceptionHandler(aros.services.rms.core.user.application.exception.SamePasswordException.class)
  public ResponseEntity<ErrorResponse> handleSamePassword(
      aros.services.rms.core.user.application.exception.SamePasswordException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage()));
  }

  // --- Missing handlers referenced in ExhaustedRetryException ---

  /** Handles StorageLocationNotFoundException. */
  @ExceptionHandler(StorageLocationNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleStorageLocationNotFound(
      StorageLocationNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  /** Handles IllegalArgumentException. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage()));
  }

  /** Handles IllegalStateException. */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  /** Handles InvalidProductOptionException. */
  @ExceptionHandler(InvalidProductOptionException.class)
  public ResponseEntity<ErrorResponse> handleInvalidProductOption(InvalidProductOptionException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage()));
  }

  /** Handles validation errors for request body. */
  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      org.springframework.web.bind.MethodArgumentNotValidException e) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Validation failed");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, message));
  }

  // --- Spring Retry ---

  /** Handles ExhaustedRetryException. */
  @ExceptionHandler(ExhaustedRetryException.class)
  public ResponseEntity<ErrorResponse> handleExhaustedRetry(ExhaustedRetryException e) {
    Throwable cause = e.getCause();
    if (cause instanceof RuntimeException runtimeException) {
      // Re-dispatch to the appropriate handler by rethrowing the real cause
      // We handle known business exceptions explicitly here
      if (cause instanceof InsufficientStockException ex) {
        return handleInsufficientStock(ex);
      }
      if (cause instanceof StorageLocationNotFoundException ex) {
        return handleStorageLocationNotFound(ex);
      }
      if (cause instanceof IllegalArgumentException ex) {
        return handleIllegalArgument(ex);
      }
      if (cause instanceof IllegalStateException ex) {
        return handleIllegalState(ex);
      }
      if (cause instanceof InvalidProductOptionException ex) {
        return handleInvalidProductOption(ex);
      }
    }
    log.error(
        "Retry agotado sin recovery: causa={}", cause != null ? cause.getMessage() : "unknown", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(500, "Error interno del servidor"));
  }

  // --- Generic catch-all handlers ---

  /** Handles generic Exception. */
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

  /** Handles Throwable. */
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
