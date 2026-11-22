/* (C) 2026 */

package aros.services.rms.infraestructure.payment.api;

import aros.services.rms.core.auth.port.output.CurrentUserPort;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.payment.application.dto.PaymentFilter;
import aros.services.rms.core.payment.application.dto.RegisterPaymentCommand;
import aros.services.rms.core.payment.application.dto.VoidPaymentCommand;
import aros.services.rms.core.payment.application.exception.InvalidPaymentException;
import aros.services.rms.core.payment.domain.DiscountType;
import aros.services.rms.core.payment.domain.Payment;
import aros.services.rms.core.payment.domain.PaymentQueryResult;
import aros.services.rms.core.payment.domain.PaymentStatus;
import aros.services.rms.core.payment.domain.PaymentTenderInput;
import aros.services.rms.core.payment.domain.PaymentTenderMethod;
import aros.services.rms.core.payment.domain.PendingOrderPage;
import aros.services.rms.core.payment.domain.RegisteredPayment;
import aros.services.rms.core.payment.port.input.PaymentQueryUseCase;
import aros.services.rms.core.payment.port.input.RegisterPaymentUseCase;
import aros.services.rms.core.payment.port.input.VoidPaymentUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.core.user.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.UserWithAreas;
import aros.services.rms.infraestructure.common.dto.PageResponse;
import aros.services.rms.infraestructure.payment.api.dto.PaymentApiMapper;
import aros.services.rms.infraestructure.payment.api.dto.PaymentResponse;
import aros.services.rms.infraestructure.payment.api.dto.PaymentSummaryResponse;
import aros.services.rms.infraestructure.payment.api.dto.PendingOrderResponse;
import aros.services.rms.infraestructure.payment.api.dto.RegisterPaymentRequest;
import aros.services.rms.infraestructure.payment.api.dto.VoidPaymentRequest;
import aros.services.rms.infraestructure.share.security.JustCashierOrAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for the minimal charge-and-close payments slice. */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Charge, void and query customer payments")
@JustCashierOrAdmin
public class PaymentController {

  private final RegisterPaymentUseCase registerPaymentUseCase;
  private final VoidPaymentUseCase voidPaymentUseCase;
  private final PaymentQueryUseCase paymentQueryUseCase;
  private final PaymentApiMapper paymentApiMapper;
  private final CurrentUserPort currentUserPort;
  private final CurrencyProvider currencyProvider;

  /**
   * Charges a delivered order.
   *
   * @param idempotencyKey the client key for this charge attempt
   * @param request the charge body
   * @return the created or replayed payment
   */
  @Operation(
      summary = "Charge an order",
      description =
          "Computes the subtotal server-side and accepts split tenders, one discount and a tip. "
              + "Requires an open cash shift. Replaying the same Idempotency-Key returns the "
              + "original payment.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Payment created"),
        @ApiResponse(
            responseCode = "200",
            description = "Idempotent replay of an existing payment"),
        @ApiResponse(responseCode = "400", description = "Invalid payment data"),
        @ApiResponse(responseCode = "403", description = "Not a cashier"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "409", description = "Order not payable or already paid"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<PaymentResponse> register(
      @Parameter(description = "Client-generated idempotency key", required = true)
          @RequestHeader(name = "Idempotency-Key", required = false)
          String idempotencyKey,
      @Valid @RequestBody RegisterPaymentRequest request) {

    RegisterPaymentCommand.DiscountCommand discount = null;
    Currency currency = currencyProvider.getCurrency();
    if (request.discount() != null) {
      DiscountType type = parseDiscountType(request.discount().type());
      int basisPoints =
          type == DiscountType.PERCENT ? toBasisPoints(request.discount().value()) : 0;
      Money amount =
          type == DiscountType.AMOUNT ? new Money(request.discount().value(), currency) : null;
      discount =
          new RegisterPaymentCommand.DiscountCommand(
              type, basisPoints, amount, request.discount().reason());
    }

    List<PaymentTenderInput> tenders =
        request.tenders().stream()
            .map(
                tender ->
                    new PaymentTenderInput(
                        parseTenderMethod(tender.method()),
                        new Money(tender.amount(), currency),
                        tender.reference()))
            .toList();

    Money tip =
        request.tipAmount() != null
            ? new Money(request.tipAmount(), currency)
            : Money.zero(currency);

    RegisterPaymentCommand command =
        new RegisterPaymentCommand(
            request.orderId(), tip, discount, tenders, idempotencyKey, currentUserId());

    RegisteredPayment result = registerPaymentUseCase.register(command);
    PaymentResponse body = paymentApiMapper.toResponse(result.payment());
    return ResponseEntity.status(result.replayed() ? HttpStatus.OK : HttpStatus.CREATED).body(body);
  }

  /**
   * Voids a completed payment.
   *
   * @param id the payment id
   * @param request the void reason
   * @return the voided payment
   */
  @Operation(
      summary = "Void a payment",
      description = "Full void only, with a mandatory reason, while the owning shift is open.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Payment voided"),
        @ApiResponse(responseCode = "400", description = "Missing reason"),
        @ApiResponse(responseCode = "403", description = "Not a cashier"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "409", description = "Already voided or shift closed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/{id}/void")
  public ResponseEntity<PaymentResponse> voidPayment(
      @Parameter(description = "Payment ID", required = true) @PathVariable Long id,
      @Valid @RequestBody VoidPaymentRequest request) {
    Payment payment =
        voidPaymentUseCase.voidPayment(
            new VoidPaymentCommand(id, request.reason(), currentUserId()));
    return ResponseEntity.ok(paymentApiMapper.toResponse(payment));
  }

  /**
   * Lists delivered orders pending payment for a business date.
   *
   * @param date the business date, defaults to today
   * @param search optional exact order id or table number
   * @param page zero-based page
   * @param size page size
   * @return one page of pending orders
   */
  @Operation(
      summary = "List pending orders",
      description =
          "Returns DELIVERED orders without an active payment for the business date. "
              + "A voided payment makes the order pending again with paymentStatus VOIDED.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Pending orders retrieved"),
        @ApiResponse(responseCode = "403", description = "Not a cashier"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/pending")
  public ResponseEntity<PageResponse<PendingOrderResponse>> pending(
      @Parameter(description = "Business date (YYYY-MM-DD)", example = "2026-11-21")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      @Parameter(description = "Exact order id or table number", example = "3")
          @RequestParam(required = false)
          String search,
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
    PendingOrderPage result = paymentQueryUseCase.findPending(date, search, page, size);
    List<PendingOrderResponse> items =
        result.items().stream().map(paymentApiMapper::toPending).toList();
    return ResponseEntity.ok(PageResponse.of(items, result.total(), result.page(), result.size()));
  }

  /**
   * Returns a payment by id.
   *
   * @param id the payment id
   * @return the payment and receipt data
   */
  @Operation(
      summary = "Get a payment",
      description = "Returns the payment detail and receipt data.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Payment retrieved"),
        @ApiResponse(responseCode = "403", description = "Not a cashier"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<PaymentResponse> findById(
      @Parameter(description = "Payment ID", required = true) @PathVariable Long id) {
    return ResponseEntity.ok(paymentApiMapper.toResponse(paymentQueryUseCase.findById(id)));
  }

  /**
   * Lists the payment journal.
   *
   * @param date business date filter
   * @param method tender method filter
   * @param status payment status filter
   * @param page zero-based page
   * @param size page size
   * @return one page of payment summaries
   */
  @Operation(
      summary = "List payments",
      description = "Paged payment journal with optional date, method and status filters.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved"),
        @ApiResponse(responseCode = "400", description = "Invalid filter value"),
        @ApiResponse(responseCode = "403", description = "Not a cashier"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<PageResponse<PaymentSummaryResponse>> search(
      @Parameter(description = "Business date (YYYY-MM-DD)")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      @Parameter(description = "Tender method (CASH, CARD, TRANSFER)")
          @RequestParam(required = false)
          String method,
      @Parameter(description = "Payment status (COMPLETED, VOIDED)") @RequestParam(required = false)
          String status,
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
    PaymentFilter filter =
        new PaymentFilter(
            date,
            method != null && !method.isBlank() ? parseTenderMethod(method) : null,
            status != null && !status.isBlank() ? parseStatus(status) : null,
            page,
            size);
    PaymentQueryResult result = paymentQueryUseCase.search(filter);
    List<PaymentSummaryResponse> items =
        result.items().stream().map(paymentApiMapper::toSummary).toList();
    return ResponseEntity.ok(PageResponse.of(items, result.total(), result.page(), result.size()));
  }

  private Long currentUserId() {
    return currentUserPort
        .fetchCurrentUserInfo()
        .map(UserWithAreas::id)
        .map(userId -> userId.value())
        .orElseThrow(
            () -> new UserNotFoundException("The authenticated cashier could not be resolved"));
  }

  private static PaymentTenderMethod parseTenderMethod(String value) {
    try {
      return PaymentTenderMethod.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new InvalidPaymentException("Unsupported tender method: " + value);
    }
  }

  private static DiscountType parseDiscountType(String value) {
    try {
      return DiscountType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new InvalidPaymentException("Unsupported discount type: " + value);
    }
  }

  private static int toBasisPoints(BigDecimal value) {
    try {
      return value.movePointRight(2).intValueExact();
    } catch (ArithmeticException e) {
      throw new InvalidPaymentException(
          "The discount percentage must have at most two decimal places");
    }
  }

  private static PaymentStatus parseStatus(String value) {
    try {
      return PaymentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new InvalidPaymentException("Unsupported payment status: " + value);
    }
  }
}
