/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;
import aros.services.rms.core.auth.application.exception.InvalidRefreshTokenException;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.image.application.exception.ImageNotFoundException;
import aros.services.rms.core.image.application.exception.ImageUploadException;
import aros.services.rms.core.image.application.exception.InvalidImageException;
import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.application.exception.StorageLocationNotFoundException;
import aros.services.rms.core.inventory.application.exception.SupplyAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyCategoryAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyVariantAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyVariantNotFoundException;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.application.exception.TableNotAvailableException;
import aros.services.rms.core.product.application.exception.InvalidProductOptionException;
import aros.services.rms.core.schedule.application.exception.ScheduleAlreadyExistsException;
import aros.services.rms.core.schedule.application.exception.ScheduleHasAssignmentsException;
import aros.services.rms.core.schedule.application.exception.ScheduleNotFoundException;
import aros.services.rms.core.schedule.application.exception.ShiftOverlapException;
import aros.services.rms.core.schedule.application.exception.WorkerNotInShiftException;
import aros.services.rms.core.schedule.application.exception.WorkerScheduleAssignmentNotFoundException;
import aros.services.rms.core.table.application.exception.InvalidTableStatusException;
import aros.services.rms.core.user.application.exception.InvalidSalaryException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

  // --- Auth exceptions ---

  /** Handles InvalidCredentialsException. */
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidCredentials(
      InvalidCredentialsException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("status", 401, "message", e.getMessage()));
  }

  /** Handles UserNotFoundException. */
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("status", 400, "message", e.getMessage()));
  }

  /** Handles InvalidRefreshTokenException. */
  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidRefreshTokenException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("status", 401, "message", e.getMessage()));
  }

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

  /** Handles UserNotFoundException. */
  @ExceptionHandler(aros.services.rms.core.user.application.exception.UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFound(
      aros.services.rms.core.user.application.exception.UserNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  /** Handles UserNotFoundByEmailException. */
  @ExceptionHandler(
      aros.services.rms.core.user.application.exception.UserNotFoundByEmailException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFoundByEmail(
      aros.services.rms.core.user.application.exception.UserNotFoundByEmailException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage()));
  }

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

  /** Handles InvalidSalaryException. */
  @ExceptionHandler(InvalidSalaryException.class)
  public ResponseEntity<ErrorResponse> handleInvalidSalary(InvalidSalaryException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage()));
  }

  // --- Schedule exceptions ---

  /** Handles ScheduleNotFoundException. */
  @ExceptionHandler(ScheduleNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleScheduleNotFound(ScheduleNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  /** Handles ScheduleAlreadyExistsException. */
  @ExceptionHandler(ScheduleAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleScheduleAlreadyExists(
      ScheduleAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  /** Handles ShiftOverlapException. */
  @ExceptionHandler(ShiftOverlapException.class)
  public ResponseEntity<ErrorResponse> handleShiftOverlap(ShiftOverlapException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  /** Handles ScheduleHasAssignmentsException. */
  @ExceptionHandler(ScheduleHasAssignmentsException.class)
  public ResponseEntity<ErrorResponse> handleScheduleHasAssignments(
      ScheduleHasAssignmentsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, e.getMessage()));
  }

  /** Handles WorkerNotInShiftException. */
  @ExceptionHandler(WorkerNotInShiftException.class)
  public ResponseEntity<ErrorResponse> handleWorkerNotInShift(WorkerNotInShiftException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(403, e.getMessage()));
  }

  /** Handles WorkerScheduleAssignmentNotFoundException. */
  @ExceptionHandler(WorkerScheduleAssignmentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleWorkerScheduleAssignmentNotFound(
      WorkerScheduleAssignmentNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
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
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(400, message));
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

  // --- Image exceptions ---

  /** Handles ImageNotFoundException. */
  @ExceptionHandler(ImageNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleImageNotFound(ImageNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  /** Handles InvalidImageException. */
  @ExceptionHandler(InvalidImageException.class)
  public ResponseEntity<ErrorResponse> handleInvalidImage(InvalidImageException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, e.getMessage()));
  }

  /** Handles ImageUploadException. */
  @ExceptionHandler(ImageUploadException.class)
  public ResponseEntity<ErrorResponse> handleImageUpload(ImageUploadException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(500, e.getMessage()));
  }

  /** Handles MaxUploadSizeExceededException. */
  @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
      org.springframework.web.multipart.MaxUploadSizeExceededException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, "File size exceeds maximum allowed size"));
  }

  // --- Generic catch-all handlers ---

  /** Handles MethodArgumentTypeMismatchException - invalid enum/path variable values. */
  @ExceptionHandler(
      org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
      org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e) {
    String message = "Invalid value '" + e.getName() + "': " + e.getValue();
    log.warn("Invalid parameter: {}", message);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, message));
  }

  /** Handles HttpMediaTypeNotAcceptableException - content negotiation failures. */
  @ExceptionHandler(org.springframework.web.HttpMediaTypeNotAcceptableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotAcceptable(
      org.springframework.web.HttpMediaTypeNotAcceptableException e) {
    log.warn("Unsupported media type: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
        .body(new ErrorResponse(406, "Content type not accepted"));
  }

  /** Handles MissingServletRequestParameterException. */
  @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
      org.springframework.web.bind.MissingServletRequestParameterException e) {
    log.warn("Missing required parameter: {}", e.getParameterName());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            new ErrorResponse(400, "Required parameter '" + e.getParameterName() + "' not found"));
  }

  /** Handles NoResourceFoundException - for requests to non-existent endpoints. */
  @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(
      org.springframework.web.servlet.resource.NoResourceFoundException e) {
    String path = e.getResourcePath();
    String message;
    if (path.contains("/products/") || path.contains("/users/")) {
      message =
          "Invalid image endpoint format."
              + " Use: /api/v1/products/{productId}/images or /api/v1/users/{userId}/images";
    } else {
      message = "Endpoint not found: " + path;
    }
    log.warn("Resource not found: {}", path);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, message));
  }

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
