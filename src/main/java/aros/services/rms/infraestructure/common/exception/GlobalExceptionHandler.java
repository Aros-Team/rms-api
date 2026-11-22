/* (C) 2026 */

package aros.services.rms.infraestructure.common.exception;

import aros.services.rms.core.analytics.domain.exception.AnalyticsConfigNotFoundException;
import aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsConfigException;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;
import aros.services.rms.core.auth.application.exception.InvalidRefreshTokenException;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.cashshift.application.exception.CashShiftNotFoundException;
import aros.services.rms.core.cashshift.application.exception.InvalidCashShiftException;
import aros.services.rms.core.cashshift.application.exception.ShiftAlreadyOpenException;
import aros.services.rms.core.cashshift.application.exception.ShiftClosedException;
import aros.services.rms.core.cashshift.application.exception.ShiftNotOpenException;
import aros.services.rms.core.image.application.exception.ImageNotFoundException;
import aros.services.rms.core.image.application.exception.ImageUploadException;
import aros.services.rms.core.image.application.exception.InvalidImageException;
import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.application.exception.StorageLocationNotFoundException;
import aros.services.rms.core.inventory.application.exception.SupplyAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyCategoryAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyNotFoundException;
import aros.services.rms.core.inventory.application.exception.SupplyVariantAlreadyExistsException;
import aros.services.rms.core.inventory.application.exception.SupplyVariantNotFoundException;
import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.application.exception.SingleChoiceOptionGroupLimitException;
import aros.services.rms.core.order.application.exception.TableNotAvailableException;
import aros.services.rms.core.payment.application.exception.InvalidPaymentException;
import aros.services.rms.core.payment.application.exception.OrderAlreadyPaidException;
import aros.services.rms.core.payment.application.exception.OrderNotPayableException;
import aros.services.rms.core.payment.application.exception.PaymentAlreadyVoidedException;
import aros.services.rms.core.payment.application.exception.PaymentNotFoundException;
import aros.services.rms.core.payroll.domain.exception.InvalidPayrollPeriodException;
import aros.services.rms.core.payroll.domain.exception.PayrollAlreadyExistsException;
import aros.services.rms.core.payroll.domain.exception.PayrollEventNotFoundException;
import aros.services.rms.core.payroll.domain.exception.PayrollImmutableException;
import aros.services.rms.core.payroll.domain.exception.PayrollNotFoundException;
import aros.services.rms.core.product.application.exception.InvalidProductOptionException;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.purchase.application.exception.InvalidPurchaseItemException;
import aros.services.rms.core.purchase.application.exception.PurchaseOrderNotFoundException;
import aros.services.rms.core.purchase.application.exception.SupplierInactiveException;
import aros.services.rms.core.purchase.application.exception.SupplierNotFoundException;
import aros.services.rms.core.schedule.application.exception.ScheduleAlreadyExistsException;
import aros.services.rms.core.schedule.application.exception.ScheduleHasAssignmentsException;
import aros.services.rms.core.schedule.application.exception.ScheduleNotFoundException;
import aros.services.rms.core.schedule.application.exception.ShiftOverlapException;
import aros.services.rms.core.schedule.application.exception.WorkerNotInShiftException;
import aros.services.rms.core.schedule.application.exception.WorkerScheduleAssignmentNotFoundException;
import aros.services.rms.core.specialselection.application.exception.InvalidSpecialSelectionException;
import aros.services.rms.core.specialselection.application.exception.InvalidSpecialSelectionHistoryException;
import aros.services.rms.core.specialselection.application.exception.InvalidSpecialSelectionScheduleException;
import aros.services.rms.core.specialselection.application.exception.SpecialSelectionNotAvailableException;
import aros.services.rms.core.specialselection.application.exception.SpecialSelectionNotFoundException;
import aros.services.rms.core.specialselection.application.exception.SupplyVariantUnitCostMissingException;
import aros.services.rms.core.systemconfig.domain.exception.SettingsException;
import aros.services.rms.core.table.application.exception.InvalidTableStatusException;
import aros.services.rms.core.user.application.exception.InvalidSalaryException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler that maps core exceptions to RFC 7807 problem responses.
 *
 * <p>Every handler returns {@code application/problem+json} ({@link ProblemDetail}) with the same
 * HTTP status as before. The body exposes {@code type}, {@code title}, {@code status} and {@code
 * detail}; it is never wrapped in the success envelope.
 */
@RestControllerAdvice
@Order(10)
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Builds a problem response for the given status and detail.
   *
   * @param status the HTTP status
   * @param detail human-readable explanation
   * @return a {@code application/problem+json} response
   */
  private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
    ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
    body.setTitle(status.getReasonPhrase());
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(body);
  }

  /**
   * Builds a problem response carrying extra RFC 7807 extension properties.
   *
   * @param status the HTTP status
   * @param detail human-readable explanation
   * @param properties extension members added to the problem body
   * @return a {@code application/problem+json} response
   */
  private static ResponseEntity<ProblemDetail> problem(
      HttpStatus status, String detail, Map<String, Object> properties) {
    ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
    body.setTitle(status.getReasonPhrase());
    properties.forEach(body::setProperty);
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(body);
  }

  /**
   * Handles settings failures, mapping the domain category to an RFC 7807 response.
   *
   * @param e the settings exception
   * @return a problem response with 400, 404 or 409 status
   */
  @ExceptionHandler(SettingsException.class)
  public ResponseEntity<ProblemDetail> handleSettings(SettingsException e) {
    HttpStatus status =
        switch (e.getErrorType()) {
          case NOT_FOUND -> HttpStatus.NOT_FOUND;
          case CONFLICT -> HttpStatus.CONFLICT;
          case INVALID -> HttpStatus.BAD_REQUEST;
        };
    return problem(status, e.getMessage());
  }

  // --- Auth exceptions ---

  /** Handles InvalidCredentialsException. */
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException e) {
    return problem(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  /** Handles UserInactiveException (an inactive account cannot authenticate). */
  @ExceptionHandler(aros.services.rms.core.auth.application.exception.UserInactiveException.class)
  public ResponseEntity<ProblemDetail> handleUserInactive(
      aros.services.rms.core.auth.application.exception.UserInactiveException e) {
    return problem(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  /** Handles UserDeletedException (a soft-deleted account cannot authenticate). */
  @ExceptionHandler(aros.services.rms.core.user.application.exception.UserDeletedException.class)
  public ResponseEntity<ProblemDetail> handleUserDeleted(
      aros.services.rms.core.user.application.exception.UserDeletedException e) {
    return problem(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  /** Handles UserNotFoundException. */
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleUserNotFound(UserNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles UserNotFoundException (user domain). */
  @ExceptionHandler(aros.services.rms.core.user.application.exception.UserNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleUserNotFound(
      aros.services.rms.core.user.application.exception.UserNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles PasswordResetTokenInvalidException. */
  @ExceptionHandler(
      aros.services.rms.core.auth.application.exception.PasswordResetTokenInvalidException.class)
  public ResponseEntity<ProblemDetail> handlePasswordResetTokenInvalid(
      aros.services.rms.core.auth.application.exception.PasswordResetTokenInvalidException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles PasswordResetTokenExpiredException. */
  @ExceptionHandler(
      aros.services.rms.core.auth.application.exception.PasswordResetTokenExpiredException.class)
  public ResponseEntity<ProblemDetail> handlePasswordResetTokenExpired(
      aros.services.rms.core.auth.application.exception.PasswordResetTokenExpiredException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles AccountSetupTokenInvalidException. */
  @ExceptionHandler(
      aros.services.rms.core.auth.application.exception.AccountSetupTokenInvalidException.class)
  public ResponseEntity<ProblemDetail> handleAccountSetupTokenInvalid(
      aros.services.rms.core.auth.application.exception.AccountSetupTokenInvalidException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles AccountSetupTokenExpiredException. */
  @ExceptionHandler(
      aros.services.rms.core.auth.application.exception.AccountSetupTokenExpiredException.class)
  public ResponseEntity<ProblemDetail> handleAccountSetupTokenExpired(
      aros.services.rms.core.auth.application.exception.AccountSetupTokenExpiredException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles InvalidRefreshTokenException. */
  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ResponseEntity<ProblemDetail> handleInvalidToken(InvalidRefreshTokenException e) {
    return problem(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  // --- Order exceptions ---

  /** Handles OrderNotFoundException. */
  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleOrderNotFound(OrderNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles ProductNotFoundException in order context. */
  @ExceptionHandler(
      aros.services.rms.core.order.application.exception.ProductNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleOrderProductNotFound(
      aros.services.rms.core.order.application.exception.ProductNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles ProductNotFoundException in the product context. */
  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleProductNotFound(ProductNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /**
   * Handles TableNotAvailableException.
   *
   * @param e the exception
   * @return problem response
   */
  @ExceptionHandler(TableNotAvailableException.class)
  public ResponseEntity<ProblemDetail> handleTableNotAvailable(TableNotAvailableException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles InvalidTableStatusException. */
  @ExceptionHandler(InvalidTableStatusException.class)
  public ResponseEntity<ProblemDetail> handleInvalidTableStatus(InvalidTableStatusException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  // --- Payment exceptions ---

  /** Handles InvalidPaymentException. */
  @ExceptionHandler(InvalidPaymentException.class)
  public ResponseEntity<ProblemDetail> handleInvalidPayment(InvalidPaymentException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles PaymentNotFoundException. */
  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ProblemDetail> handlePaymentNotFound(PaymentNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles OrderNotPayableException. */
  @ExceptionHandler(OrderNotPayableException.class)
  public ResponseEntity<ProblemDetail> handleOrderNotPayable(OrderNotPayableException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles OrderAlreadyPaidException, exposing the blocking payment id. */
  @ExceptionHandler(OrderAlreadyPaidException.class)
  public ResponseEntity<ProblemDetail> handleOrderAlreadyPaid(OrderAlreadyPaidException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage(), Map.of("paymentId", e.getPaymentId()));
  }

  /** Handles PaymentAlreadyVoidedException. */
  @ExceptionHandler(PaymentAlreadyVoidedException.class)
  public ResponseEntity<ProblemDetail> handlePaymentAlreadyVoided(PaymentAlreadyVoidedException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  // --- Cash shift exceptions ---

  /** Handles InvalidCashShiftException. */
  @ExceptionHandler(InvalidCashShiftException.class)
  public ResponseEntity<ProblemDetail> handleInvalidCashShift(InvalidCashShiftException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles CashShiftNotFoundException. */
  @ExceptionHandler(CashShiftNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleCashShiftNotFound(CashShiftNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles ShiftAlreadyOpenException. */
  @ExceptionHandler(ShiftAlreadyOpenException.class)
  public ResponseEntity<ProblemDetail> handleShiftAlreadyOpen(ShiftAlreadyOpenException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles ShiftNotOpenException. */
  @ExceptionHandler(ShiftNotOpenException.class)
  public ResponseEntity<ProblemDetail> handleShiftNotOpen(ShiftNotOpenException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles ShiftClosedException. */
  @ExceptionHandler(ShiftClosedException.class)
  public ResponseEntity<ProblemDetail> handleShiftClosed(ShiftClosedException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  // --- Inventory exceptions ---

  /** Handles SpecialSelectionNotAvailableException. */
  @ExceptionHandler(SpecialSelectionNotAvailableException.class)
  public ResponseEntity<ProblemDetail> handleSpecialSelectionNotAvailable(
      SpecialSelectionNotAvailableException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles SpecialSelectionNotFoundException. */
  @ExceptionHandler(SpecialSelectionNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleSpecialSelectionNotFound(
      SpecialSelectionNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles InvalidSpecialSelectionException with the individual validation errors. */
  @ExceptionHandler(InvalidSpecialSelectionException.class)
  public ResponseEntity<ProblemDetail> handleInvalidSpecialSelection(
      InvalidSpecialSelectionException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage(), Map.of("errors", e.getErrors()));
  }

  /** Handles InvalidSpecialSelectionHistoryException. */
  @ExceptionHandler(InvalidSpecialSelectionHistoryException.class)
  public ResponseEntity<ProblemDetail> handleInvalidSpecialSelectionHistory(
      InvalidSpecialSelectionHistoryException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles InvalidSpecialSelectionScheduleException. */
  @ExceptionHandler(InvalidSpecialSelectionScheduleException.class)
  public ResponseEntity<ProblemDetail> handleInvalidSpecialSelectionSchedule(
      InvalidSpecialSelectionScheduleException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles SupplyVariantUnitCostMissingException, exposing the offending variant ids. */
  @ExceptionHandler(SupplyVariantUnitCostMissingException.class)
  public ResponseEntity<ProblemDetail> handleSupplyVariantUnitCostMissing(
      SupplyVariantUnitCostMissingException e) {
    return problem(
        HttpStatus.UNPROCESSABLE_ENTITY,
        e.getMessage(),
        Map.of("missingVariants", e.getVariantIds()));
  }

  /** Handles InsufficientStockException. */
  @ExceptionHandler(InsufficientStockException.class)
  public ResponseEntity<ProblemDetail> handleInsufficientStock(InsufficientStockException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /**
   * Handles SupplyVariantNotFoundException.
   *
   * @param e the exception
   * @return problem response
   */
  @ExceptionHandler(SupplyVariantNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleSupplyVariantNotFound(
      SupplyVariantNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles SupplyVariantAlreadyExistsException. */
  @ExceptionHandler(SupplyVariantAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleSupplyVariantAlreadyExists(
      SupplyVariantAlreadyExistsException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles SupplyAlreadyExistsException. */
  @ExceptionHandler(SupplyAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleSupplyAlreadyExists(SupplyAlreadyExistsException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles SupplyCategoryAlreadyExistsException. */
  @ExceptionHandler(SupplyCategoryAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleSupplyCategoryAlreadyExists(
      SupplyCategoryAlreadyExistsException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles SupplyNotFoundException. */
  @ExceptionHandler(SupplyNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleSupplyNotFound(SupplyNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  // --- Purchase exceptions ---

  /** Handles SupplierNotFoundException. */
  @ExceptionHandler(SupplierNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleSupplierNotFound(SupplierNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles SupplierInactiveException. */
  @ExceptionHandler(SupplierInactiveException.class)
  public ResponseEntity<ProblemDetail> handleSupplierInactive(SupplierInactiveException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles PurchaseOrderNotFoundException. */
  @ExceptionHandler(PurchaseOrderNotFoundException.class)
  public ResponseEntity<ProblemDetail> handlePurchaseOrderNotFound(
      PurchaseOrderNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles InvalidPurchaseItemException. */
  @ExceptionHandler(InvalidPurchaseItemException.class)
  public ResponseEntity<ProblemDetail> handleInvalidPurchaseItem(InvalidPurchaseItemException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles AccountSetupTokenAlreadyUsedException. */
  @ExceptionHandler(
      aros.services.rms.core.auth.application.exception.AccountSetupTokenAlreadyUsedException.class)
  public ResponseEntity<ProblemDetail> handleAccountSetupTokenAlreadyUsed(
      aros.services.rms.core.auth.application.exception.AccountSetupTokenAlreadyUsedException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  // --- Authorization handlers ---

  /** Handles AuthorizationDeniedException. */
  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ProblemDetail> handleAccessDenied(AuthorizationDeniedException e) {
    log.warn("accesso denegado: {}", e.getMessage());
    return problem(HttpStatus.FORBIDDEN, e.getMessage());
  }

  // --- User exceptions ---

  /** Handles UserNotFoundByEmailException. */
  @ExceptionHandler(
      aros.services.rms.core.user.application.exception.UserNotFoundByEmailException.class)
  public ResponseEntity<ProblemDetail> handleUserNotFoundByEmail(
      aros.services.rms.core.user.application.exception.UserNotFoundByEmailException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles InvalidPasswordException. */
  @ExceptionHandler(
      aros.services.rms.core.user.application.exception.InvalidPasswordException.class)
  public ResponseEntity<ProblemDetail> handleInvalidPassword(
      aros.services.rms.core.user.application.exception.InvalidPasswordException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles SamePasswordException. */
  @ExceptionHandler(aros.services.rms.core.user.application.exception.SamePasswordException.class)
  public ResponseEntity<ProblemDetail> handleSamePassword(
      aros.services.rms.core.user.application.exception.SamePasswordException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles InvalidSalaryException. */
  @ExceptionHandler(InvalidSalaryException.class)
  public ResponseEntity<ProblemDetail> handleInvalidSalary(InvalidSalaryException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  // --- Schedule exceptions ---

  /** Handles ScheduleNotFoundException. */
  @ExceptionHandler(ScheduleNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleScheduleNotFound(ScheduleNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles ScheduleAlreadyExistsException. */
  @ExceptionHandler(ScheduleAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleScheduleAlreadyExists(
      ScheduleAlreadyExistsException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles ShiftOverlapException. */
  @ExceptionHandler(ShiftOverlapException.class)
  public ResponseEntity<ProblemDetail> handleShiftOverlap(ShiftOverlapException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles ScheduleHasAssignmentsException. */
  @ExceptionHandler(ScheduleHasAssignmentsException.class)
  public ResponseEntity<ProblemDetail> handleScheduleHasAssignments(
      ScheduleHasAssignmentsException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles WorkerNotInShiftException. */
  @ExceptionHandler(WorkerNotInShiftException.class)
  public ResponseEntity<ProblemDetail> handleWorkerNotInShift(WorkerNotInShiftException e) {
    return problem(HttpStatus.FORBIDDEN, e.getMessage());
  }

  /** Handles WorkerScheduleAssignmentNotFoundException. */
  @ExceptionHandler(WorkerScheduleAssignmentNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleWorkerScheduleAssignmentNotFound(
      WorkerScheduleAssignmentNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  // --- Payroll exceptions ---

  /** Handles PayrollNotFoundException. */
  @ExceptionHandler(PayrollNotFoundException.class)
  public ResponseEntity<ProblemDetail> handlePayrollNotFound(PayrollNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles PayrollEventNotFoundException. */
  @ExceptionHandler(PayrollEventNotFoundException.class)
  public ResponseEntity<ProblemDetail> handlePayrollEventNotFound(PayrollEventNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles PayrollAlreadyExistsException. */
  @ExceptionHandler(PayrollAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handlePayrollAlreadyExists(PayrollAlreadyExistsException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles PayrollImmutableException. */
  @ExceptionHandler(PayrollImmutableException.class)
  public ResponseEntity<ProblemDetail> handlePayrollImmutable(PayrollImmutableException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles InvalidPayrollPeriodException. */
  @ExceptionHandler(InvalidPayrollPeriodException.class)
  public ResponseEntity<ProblemDetail> handleInvalidPayrollPeriod(InvalidPayrollPeriodException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  // --- Analytics exceptions ---

  /** Handles AnalyticsConfigNotFoundException. */
  @ExceptionHandler(AnalyticsConfigNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleAnalyticsConfigNotFound(
      AnalyticsConfigNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles InvalidAnalyticsConfigException. */
  @ExceptionHandler(InvalidAnalyticsConfigException.class)
  public ResponseEntity<ProblemDetail> handleInvalidAnalyticsConfig(
      InvalidAnalyticsConfigException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  // --- Missing handlers referenced in ExhaustedRetryException ---

  /** Handles StorageLocationNotFoundException. */
  @ExceptionHandler(StorageLocationNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleStorageLocationNotFound(
      StorageLocationNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles IllegalArgumentException. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles IllegalStateException. */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException e) {
    return problem(HttpStatus.CONFLICT, e.getMessage());
  }

  /** Handles InvalidProductOptionException. */
  @ExceptionHandler(InvalidProductOptionException.class)
  public ResponseEntity<ProblemDetail> handleInvalidProductOption(InvalidProductOptionException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles SingleChoiceOptionGroupLimitException. */
  @ExceptionHandler(SingleChoiceOptionGroupLimitException.class)
  public ResponseEntity<ProblemDetail> handleSingleChoiceOptionGroupLimit(
      SingleChoiceOptionGroupLimitException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles OptionGroupRequiresProductException. */
  @ExceptionHandler(
      aros.services.rms.core.category.application.exception.OptionGroupRequiresProductException
          .class)
  public ResponseEntity<ProblemDetail> handleOptionGroupRequiresProduct(
      aros.services.rms.core.category.application.exception.OptionGroupRequiresProductException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles validation errors for request body. */
  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
      org.springframework.web.bind.MethodArgumentNotValidException e) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Validation failed");
    return problem(HttpStatus.BAD_REQUEST, message);
  }

  // --- Spring Retry ---

  /** Handles ExhaustedRetryException. */
  @ExceptionHandler(ExhaustedRetryException.class)
  public ResponseEntity<ProblemDetail> handleExhaustedRetry(ExhaustedRetryException e) {
    Throwable cause = e.getCause();
    if (cause instanceof RuntimeException) {
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
      if (cause instanceof SpecialSelectionNotAvailableException ex) {
        return handleSpecialSelectionNotAvailable(ex);
      }
    }
    log.error(
        "Retry agotado sin recovery: causa={}", cause != null ? cause.getMessage() : "unknown", e);
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
  }

  // --- Image exceptions ---

  /** Handles ImageNotFoundException. */
  @ExceptionHandler(ImageNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleImageNotFound(ImageNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, e.getMessage());
  }

  /** Handles InvalidImageException. */
  @ExceptionHandler(InvalidImageException.class)
  public ResponseEntity<ProblemDetail> handleInvalidImage(InvalidImageException e) {
    return problem(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Handles ImageUploadException. */
  @ExceptionHandler(ImageUploadException.class)
  public ResponseEntity<ProblemDetail> handleImageUpload(ImageUploadException e) {
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
  }

  /** Handles MaxUploadSizeExceededException. */
  @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
  public ResponseEntity<ProblemDetail> handleMaxUploadSizeExceeded(
      org.springframework.web.multipart.MaxUploadSizeExceededException e) {
    return problem(HttpStatus.BAD_REQUEST, "File size exceeds maximum allowed size");
  }

  // --- Generic catch-all handlers ---

  /** Handles MethodArgumentTypeMismatchException - invalid enum/path variable values. */
  @ExceptionHandler(
      org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(
      org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e) {
    String message = "Invalid value '" + e.getName() + "': " + e.getValue();
    log.warn("Invalid parameter: {}", message);
    return problem(HttpStatus.BAD_REQUEST, message);
  }

  /** Handles HttpMediaTypeNotAcceptableException - content negotiation failures. */
  @ExceptionHandler(org.springframework.web.HttpMediaTypeNotAcceptableException.class)
  public ResponseEntity<ProblemDetail> handleHttpMediaTypeNotAcceptable(
      org.springframework.web.HttpMediaTypeNotAcceptableException e) {
    log.warn("Unsupported media type: {}", e.getMessage());
    return problem(HttpStatus.NOT_ACCEPTABLE, "Content type not accepted");
  }

  /** Handles MissingServletRequestParameterException. */
  @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
  public ResponseEntity<ProblemDetail> handleMissingServletRequestParameter(
      org.springframework.web.bind.MissingServletRequestParameterException e) {
    log.warn("Missing required parameter: {}", e.getParameterName());
    return problem(
        HttpStatus.BAD_REQUEST, "Required parameter '" + e.getParameterName() + "' not found");
  }

  /** Handles NoResourceFoundException - for requests to non-existent endpoints. */
  @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
  public ResponseEntity<ProblemDetail> handleNoResourceFound(
      org.springframework.web.servlet.resource.NoResourceFoundException e) {
    String path = e.getResourcePath();
    if (path.endsWith(".map")
        || path.endsWith(".css")
        || path.endsWith(".js")
        || path.endsWith(".png")
        || path.endsWith(".ico")
        || path.endsWith(".svg")
        || path.endsWith(".woff")
        || path.endsWith(".woff2")) {
      return ResponseEntity.notFound().build();
    }
    String message;
    if (path.contains("/products/") || path.contains("/workers/")) {
      message =
          "Invalid image endpoint format."
              + " Use: /api/v1/products/{productId}/images or /api/v1/workers/{workerId}/images";
    } else {
      message = "Endpoint not found: " + path;
    }
    log.warn("Resource not found: {}", path);
    return problem(HttpStatus.BAD_REQUEST, message);
  }

  /** Handles generic Exception. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGenericException(Exception e) {
    log.error(
        "Error interno no esperado: mensaje={}, tipo={}",
        e.getMessage(),
        e.getClass().getName(),
        e);
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
  }

  /** Handles Throwable. */
  @ExceptionHandler(Throwable.class)
  public ResponseEntity<ProblemDetail> handleThrowable(Throwable t) {
    log.error(
        "Error crítico del sistema: mensaje={}, tipo={}",
        t.getMessage(),
        t.getClass().getName(),
        t);
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
  }
}
