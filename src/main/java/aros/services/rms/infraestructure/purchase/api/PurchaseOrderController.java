/* (C) 2026 */
package aros.services.rms.infraestructure.purchase.api;

import aros.services.rms.core.purchase.domain.PurchaseOrder;
import aros.services.rms.core.purchase.domain.PurchaseOrderItem;
import aros.services.rms.core.purchase.port.input.GetPurchaseHistoryUseCase;
import aros.services.rms.infraestructure.purchase.api.dto.PurchaseOrderRequest;
import aros.services.rms.infraestructure.purchase.api.dto.PurchaseOrderResponse;
import aros.services.rms.infraestructure.purchase.config.RegisterPurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@Tag(name = "Purchases", description = "Purchase order registration and history")
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
      summary = "Register purchase order",
      description =
          "Registers a new purchase order and automatically updates Bodega stock."
              + " Each item's quantityReceived enters the inventory as an ENTRY movement."
              + " The entire operation is atomic — if any step fails, nothing is persisted.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Purchase order registered successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or quantityReceived > quantityOrdered"),
        @ApiResponse(responseCode = "404", description = "Supplier or supply variant not found"),
        @ApiResponse(responseCode = "409", description = "Supplier is inactive")
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
                        .unitPrice(itemReq.unitPrice())
                        .build())
            .collect(Collectors.toList());

    var order =
        PurchaseOrder.builder()
            .supplierId(request.supplierId())
            .registeredById(request.registeredById())
            .purchasedAt(request.purchasedAt())
            .totalAmount(request.totalAmount())
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
   * @param from optional start date
   * @param to optional end date
   * @return the list of purchase orders
   */
  @Operation(
      summary = "List purchase history",
      description =
          "Returns all purchase orders. Optionally filter by supplierId or date range (from/to)."
              + " If supplierId is provided, date range is ignored.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Purchase orders retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date format"),
        @ApiResponse(
            responseCode = "404",
            description = "Supplier not found (when filtering by supplierId)")
      })
  @GetMapping
  public ResponseEntity<List<PurchaseOrderResponse>> findAll(
      @RequestParam(required = false) Long supplierId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

    List<PurchaseOrder> orders;

    if (supplierId != null) {
      // Filter by supplier
      orders = getPurchaseHistoryUseCase.findBySupplierId(supplierId);
    } else if (from != null && to != null) {
      // Filter by date range
      orders = getPurchaseHistoryUseCase.findByDateRange(from, to);
    } else {
      // No filter — return all
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
      summary = "Get purchase order by ID",
      description = "Returns the full detail of a single purchase order including all line items.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Purchase order retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Purchase order not found")
      })
  @GetMapping("/{id}")
  public ResponseEntity<PurchaseOrderResponse> findById(@PathVariable Long id) {
    var order = getPurchaseHistoryUseCase.findById(id);
    return ResponseEntity.ok(PurchaseOrderResponse.fromDomain(order));
  }
}
