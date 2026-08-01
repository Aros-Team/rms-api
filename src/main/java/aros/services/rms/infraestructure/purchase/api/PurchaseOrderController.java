/* (C) 2026 */

package aros.services.rms.infraestructure.purchase.api;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.purchase.domain.PurchaseOrder;
import aros.services.rms.core.purchase.domain.PurchaseOrderItem;
import aros.services.rms.core.purchase.port.input.GetPurchaseHistoryUseCase;
import aros.services.rms.infraestructure.purchase.api.dto.PurchaseOrderRequest;
import aros.services.rms.infraestructure.purchase.api.dto.PurchaseOrderResponse;
import aros.services.rms.infraestructure.purchase.config.RegisterPurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for purchase order registration and history. */
@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
@Tag(
    name = "Purchases",
    description = "Operations for registering purchase orders and querying purchase history")
public class PurchaseOrderController {

  private final RegisterPurchaseOrderService registerPurchaseOrderService;
  private final GetPurchaseHistoryUseCase getPurchaseHistoryUseCase;

  /**
   * Registers a purchase order.
   *
   * @param request the purchase order request
   * @return the registered purchase order
   */
  @Operation(
      tags = {"Purchases"},
      summary = "Register purchase order",
      description =
          "Registers a new purchase order and automatically updates Warehouse stock. "
              + "The received quantity of each item enters the inventory as an ENTRY movement. "
              + "The entire operation is atomic — if any step fails, nothing is persisted.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Purchase order registered successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or quantityReceived > quantityOrdered"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Supplier or supply variant not found"),
        @ApiResponse(responseCode = "409", description = "Supplier is inactive"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<PurchaseOrderResponse> register(
      @Valid @RequestBody PurchaseOrderRequest request) {
    // Map request items to domain objects
    var items =
        request.items().stream()
            .map(
                itemReq ->
                    PurchaseOrderItem.builder()
                        .supplyVariantId(itemReq.supplyVariantId())
                        .quantityOrdered(itemReq.quantityOrdered())
                        .quantityReceived(itemReq.quantityReceived())
                        .unitPrice(new Money(itemReq.unitPrice(), Currency.getInstance("COP")))
                        .build())
            .collect(Collectors.toList());

    var order =
        PurchaseOrder.builder()
            .supplierId(request.supplierId())
            .registeredById(request.registeredById())
            .purchasedAt(request.purchasedAt())
            .totalAmount(new Money(request.totalAmount(), Currency.getInstance("COP")))
            .notes(request.notes())
            .items(items)
            .build();

    var saved = registerPurchaseOrderService.register(order);
    return new ResponseEntity<>(PurchaseOrderResponse.fromDomain(saved), HttpStatus.CREATED);
  }

  /**
   * Lists purchase history.
   *
   * @param supplierId optional supplier filter
   * @param search optional notes or supplier name filter
   * @param from optional start date
   * @param to optional end date
   * @return the list of purchase orders
   */
  @Operation(
      tags = {"Purchases"},
      summary = "List purchase history",
      description =
          "Returns all purchase orders. Optionally filters by supplierId, search, or date range "
              + "(from/to). Precedence is supplierId, search, date range, then all orders.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Purchase orders retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date format"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(
            responseCode = "404",
            description = "Supplier not found (when filtering by supplierId)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<List<PurchaseOrderResponse>> findAll(
      @Parameter(description = "Filter by supplier ID", example = "1")
          @RequestParam(required = false)
          Long supplierId,
      @Parameter(
              description = "Optional name filter (partial, case-insensitive)",
              example = "fresh produce")
          @RequestParam(required = false)
          String search,
      @Parameter(description = "Filter by start date (from)", example = "2026-01-01")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate from,
      @Parameter(description = "Filter by end date (to)", example = "2026-12-31")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate to) {

    List<PurchaseOrder> orders;

    if (supplierId != null) {
      orders = getPurchaseHistoryUseCase.findBySupplierId(supplierId);
    } else if (search != null && !search.isBlank()) {
      orders = getPurchaseHistoryUseCase.findBySearch(search);
    } else if (from != null && to != null) {
      orders = getPurchaseHistoryUseCase.findByDateRange(from, to);
    } else {
      orders = getPurchaseHistoryUseCase.findAll();
    }

    var responses =
        orders.stream().map(PurchaseOrderResponse::fromDomain).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  /**
   * Gets a purchase order by ID.
   *
   * @param id the purchase order ID
   * @return the purchase order
   */
  @Operation(
      tags = {"Purchases"},
      summary = "Get purchase order by ID",
      description = "Returns the complete details of a purchase order, including all items.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Purchase order retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Purchase order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<PurchaseOrderResponse> findById(
      @Parameter(description = "Purchase order ID", example = "1", required = true) @PathVariable
          Long id) {
    var order = getPurchaseHistoryUseCase.findById(id);
    return ResponseEntity.ok(PurchaseOrderResponse.fromDomain(order));
  }
}
