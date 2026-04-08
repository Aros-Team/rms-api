/* (C) 2026 */
package aros.services.rms.infraestructure.common.exception;

import aros.services.rms.core.area.application.exception.AreaAlreadyExistsException;
import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;
import aros.services.rms.core.auth.application.exception.PasswordResetTokenExpiredException;
import aros.services.rms.core.auth.application.exception.PasswordResetTokenInvalidException;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.category.application.exception.CategoryNotFoundException;
import aros.services.rms.core.category.application.exception.OptionCategoryNotFoundException;
import aros.services.rms.core.daymenu.application.exception.InvalidDayMenuProductException;
import aros.services.rms.core.daymenu.application.exception.UnauthenticatedOperationException;
import aros.services.rms.core.email.application.exception.EmailServiceException;
import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.application.exception.StorageLocationNotFoundException;
import aros.services.rms.core.inventory.application.exception.SupplyAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyCategoryAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyNotFoundException;
import aros.services.rms.core.inventory.application.exception.SupplyVariantAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyVariantNotFoundException;
import aros.services.rms.core.order.application.exception.InvalidOrderStatusException;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.application.exception.TableNotAvailableException;
import aros.services.rms.core.product.application.exception.InvalidProductOptionException;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.product.application.exception.ProductOptionNotFoundException;
import aros.services.rms.core.purchase.application.exception.InvalidPurchaseItemException;
import aros.services.rms.core.purchase.application.exception.PurchaseOrderNotFoundException;
import aros.services.rms.core.purchase.application.exception.SupplierInactiveException;
import aros.services.rms.core.purchase.application.exception.SupplierNotFoundException;
import aros.services.rms.core.table.application.exception.InvalidTableStatusException;
import aros.services.rms.core.table.application.exception.TableNotFoundException;
import aros.services.rms.infraestructure.auth.exception.JwtKeysMissingException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.ExhaustedRetryException;
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

  // --- Day Menu exceptions ---

  @ExceptionHandler(InvalidDayMenuProductException.class)
  public ResponseEntity<ErrorResponse> handleInvalidDayMenuProduct(
      InvalidDayMenuProductException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage()));
  }

  @ExceptionHandler(UnauthenticatedOperationException.class)
  public ResponseEntity<ErrorResponse> handleUnauthenticatedOperation(
      UnauthenticatedOperationException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse(401, e.getMessage()));
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

  @ExceptionHandler(InvalidProductOptionException.class)
  public ResponseEntity<ErrorResponse> handleInvalidProductOption(InvalidProductOptionException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage()));
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

  // --- Inventory exceptions ---

  @ExceptionHandler(InsufficientStockException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  @ExceptionHandler(SupplyVariantNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleSupplyVariantNotFound(
      SupplyVariantNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  @ExceptionHandler(SupplyVariantAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleSupplyVariantAlreadyExists(
      SupplyVariantAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  @ExceptionHandler(SupplyAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleSupplyAlreadyExists(SupplyAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  @ExceptionHandler(SupplyCategoryAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleSupplyCategoryAlreadyExists(
      SupplyCategoryAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  @ExceptionHandler(SupplyNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleSupplyNotFound(SupplyNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  @ExceptionHandler(StorageLocationNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleStorageLocationNotFound(
      StorageLocationNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  // --- Purchase exceptions ---

  @ExceptionHandler(SupplierNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleSupplierNotFound(SupplierNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  @ExceptionHandler(PurchaseOrderNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePurchaseOrderNotFound(
      PurchaseOrderNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  @ExceptionHandler(SupplierInactiveException.class)
  public ResponseEntity<ErrorResponse> handleSupplierInactive(SupplierInactiveException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  @ExceptionHandler(InvalidPurchaseItemException.class)
  public ResponseEntity<ErrorResponse> handleInvalidPurchaseItem(InvalidPurchaseItemException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage()));
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
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(
            Map.of(
                "error", "Invalid credentials",
                "message", "Usuario o contraseña incorrectos"));
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
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

  @ExceptionHandler(EmailServiceException.class)
  public ResponseEntity<ErrorResponse> handleEmailServiceException(EmailServiceException e) {
    log.error("Email service error: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(
            new ErrorResponse(
                503, "Servicio de correo no disponible. Por favor, intente más tarde."));
  }

  // --- Password reset exceptions ---

  @ExceptionHandler(PasswordResetTokenInvalidException.class)
  public ResponseEntity<ErrorResponse> handlePasswordResetTokenInvalid(
      PasswordResetTokenInvalidException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage()));
  }

  @ExceptionHandler(PasswordResetTokenExpiredException.class)
  public ResponseEntity<ErrorResponse> handlePasswordResetTokenExpired(
      PasswordResetTokenExpiredException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage()));
  }

  // --- Authorization handlers ---

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AuthorizationDeniedException e) {
    log.warn("accesso denegado: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
  }

  // --- User exceptions ---

  @ExceptionHandler(
      aros.services.rms.core.user.application.exception.InvalidPasswordException.class)
  public ResponseEntity<ErrorResponse> handleInvalidPassword(
      aros.services.rms.core.user.application.exception.InvalidPasswordException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage()));
  }

  // --- Spring Retry ---

  @ExceptionHandler(ExhaustedRetryException.class)
  public ResponseEntity<ErrorResponse> handleExhaustedRetry(ExhaustedRetryException e) {
    Throwable cause = e.getCause();
    if (cause instanceof RuntimeException runtimeException) {
      // Re-dispatch to the appropriate handler by rethrowing the real cause
      // We handle known business exceptions explicitly here
      if (cause instanceof InsufficientStockException ex) {
        return handleInsufficientStock(ex);
      }
      if (cause instanceof IllegalArgumentException ex) {
        return handleIllegalArgument(ex);
      }
      if (cause instanceof IllegalStateException ex) {
        return handleIllegalState(ex);
      }
    }
    log.error(
        "Retry agotado sin recovery: causa={}", cause != null ? cause.getMessage() : "unknown", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(500, "Error interno del servidor"));
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
