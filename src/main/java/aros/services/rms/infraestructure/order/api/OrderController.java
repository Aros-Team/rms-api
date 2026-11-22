/* (C) 2026 */

package aros.services.rms.infraestructure.order.api;

import aros.services.rms.core.order.application.dto.TakeOrderCommand;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderQueryResult;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.DeliveryUseCase;
import aros.services.rms.core.order.port.input.MarkAsReadyUseCase;
import aros.services.rms.core.order.port.input.OrderQueryUseCase;
import aros.services.rms.core.order.port.input.PreparationUseCase;
import aros.services.rms.core.order.port.input.TakeOrderUseCase;
import aros.services.rms.core.order.port.input.UpdateOrderUseCase;
import aros.services.rms.infraestructure.common.dto.PageResponse;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse;
import aros.services.rms.infraestructure.order.api.dto.OrderResponseMapper;
import aros.services.rms.infraestructure.order.api.dto.TakeOrderRequest;
import aros.services.rms.infraestructure.share.security.JustKitchenOrAdmin;
import aros.services.rms.infraestructure.share.security.JustOperationsStaff;
import aros.services.rms.infraestructure.share.security.JustWaiterOrAdmin;
import aros.services.rms.infraestructure.table.api.TableNotificationService;
import aros.services.rms.infraestructure.table.api.dto.TableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for order management. */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(
    name = "Orders",
    description =
        "Operations for the full order lifecycle: creation, preparation, delivery and queries")
public class OrderController {

  private final TakeOrderUseCase takeOrderUseCase;
  private final UpdateOrderUseCase updateOrderUseCase;
  private final PreparationUseCase preparationUseCase;
  private final MarkAsReadyUseCase markAsReadyUseCase;
  private final DeliveryUseCase deliveryUseCase;
  private final OrderQueryUseCase orderQueryUseCase;
  private final OrderNotificationService orderNotificationService;
  private final TableNotificationService tableNotificationService;
  private final OrderResponseMapper orderResponseMapper;

  /**
   * Takes a new order.
   *
   * @param request the take order request
   * @return the created order
   */
  @Operation(
      tags = {"Orders"},
      summary = "Create new order",
      description = "Creates a new order with status QUEUE. Requires an available table.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Order created successfully",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Table or product not found"),
        @ApiResponse(responseCode = "409", description = "Table not available"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  @JustWaiterOrAdmin
  public ResponseEntity<OrderResponse> takeOrder(@Valid @RequestBody TakeOrderRequest request) {
    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(request.tableId())
            .partySize(request.partySize())
            .details(
                request.details().stream()
                    .map(
                        detail ->
                            TakeOrderCommand.OrderDetailCommand.builder()
                                .productId(detail.productId())
                                .instructions(detail.instructions())
                                .selectedOptionIds(detail.selectedOptionIds())
                                .selectedProductIds(detail.selectedProductIds())
                                .additionIds(detail.additionIds())
                                .clarifications(detail.clarifications())
                                .build())
                    .collect(Collectors.toList()))
            .build();

    Order order = takeOrderUseCase.execute(command);
    OrderResponse response = orderResponseMapper.toResponse(order);
    orderNotificationService.notifyOrderCreated(response);
    if (order.getTable() != null) {
      tableNotificationService.notifyTableStatusChanged(TableResponse.fromDomain(order.getTable()));
    }
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  /**
   * Cancels an order.
   *
   * @param id the order ID
   * @return the cancelled order
   */
  @Operation(
      tags = {"Orders"},
      summary = "Cancel order",
      description = "Cancels an existing order that is in QUEUE status.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Order cancelled successfully",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "409", description = "Order cannot be cancelled"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}/cancel")
  @JustWaiterOrAdmin
  public ResponseEntity<OrderResponse> cancelOrder(
      @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id) {
    Order order = updateOrderUseCase.cancel(id);
    OrderResponse response = orderResponseMapper.toResponse(order);
    orderNotificationService.notifyOrderCancelled(response);
    if (order.getTable() != null) {
      tableNotificationService.notifyTableStatusChanged(TableResponse.fromDomain(order.getTable()));
    }
    return ResponseEntity.ok(response);
  }

  /**
   * Updates order details.
   *
   * @param id the order ID
   * @param request the update request
   * @return the updated order
   */
  @Operation(
      tags = {"Orders"},
      summary = "Update order details",
      description = "Updates the details of an order in QUEUE status.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Order updated successfully",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Order or product not found"),
        @ApiResponse(responseCode = "409", description = "Order cannot be updated"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}")
  @JustWaiterOrAdmin
  public ResponseEntity<OrderResponse> updateOrder(
      @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id,
      @Valid @RequestBody TakeOrderRequest request) {
    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(request.tableId())
            .partySize(request.partySize())
            .details(
                request.details().stream()
                    .map(
                        detail ->
                            TakeOrderCommand.OrderDetailCommand.builder()
                                .productId(detail.productId())
                                .instructions(detail.instructions())
                                .selectedOptionIds(detail.selectedOptionIds())
                                .selectedProductIds(detail.selectedProductIds())
                                .additionIds(detail.additionIds())
                                .clarifications(detail.clarifications())
                                .build())
                    .collect(Collectors.toList()))
            .build();

    Order order = updateOrderUseCase.update(id, command);
    return ResponseEntity.ok(orderResponseMapper.toResponse(order));
  }

  /**
   * Processes the next order.
   *
   * @return the order being prepared
   */
  @Operation(
      tags = {"Orders"},
      summary = "Process next order",
      description = "Takes the oldest order from the queue and changes its status to PREPARING.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Order moved to preparation",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "No order found in queue"),
        @ApiResponse(responseCode = "409", description = "No orders in queue"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/prepare")
  @JustKitchenOrAdmin
  public ResponseEntity<OrderResponse> prepareNextOrder() {
    Order order = preparationUseCase.processNextOrder();
    OrderResponse response = orderResponseMapper.toResponse(order);
    orderNotificationService.notifyOrderPreparing(response);
    return ResponseEntity.ok(response);
  }

  /**
   * Starts preparation of a specific order.
   *
   * @param id the order ID
   * @return the order moved to PREPARING
   */
  @Operation(
      tags = {"Orders"},
      summary = "Prepare a specific order",
      description =
          "Moves a specific QUEUE order to PREPARING. Unlike the global FIFO endpoint, this lets"
              + " a station start the ticket it is actually working on.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Order moved to preparation",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "409", description = "Order is not in QUEUE status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}/prepare")
  @JustKitchenOrAdmin
  public ResponseEntity<OrderResponse> prepareOrder(
      @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id) {
    Order order = preparationUseCase.prepareOrder(id);
    OrderResponse response = orderResponseMapper.toResponse(order);
    orderNotificationService.notifyOrderPreparing(response);
    return ResponseEntity.ok(response);
  }

  /**
   * Marks an order as ready.
   *
   * @param id the order ID
   * @return the ready order
   */
  @Operation(
      tags = {"Orders"},
      summary = "Mark order as ready",
      description =
          "Marks a specific order as READY for delivery."
              + " Changes status from PREPARING to READY.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Order marked as ready",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "409", description = "Order is not in PREPARING status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}/ready")
  @JustKitchenOrAdmin
  public ResponseEntity<OrderResponse> markOrderAsReady(
      @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id) {
    Order order = markAsReadyUseCase.markAsReady(id);
    OrderResponse response = orderResponseMapper.toResponse(order);
    orderNotificationService.notifyOrderReady(response);
    return ResponseEntity.ok(response);
  }

  /**
   * Marks a single order line as ready at its station.
   *
   * @param id the order ID
   * @param detailId the order detail ID
   * @return the updated order, already READY when every line is ready
   */
  @Operation(
      tags = {"Orders"},
      summary = "Mark an order line as ready",
      description =
          "Marks one line of a PREPARING order as ready at its station. When every line is ready"
              + " the order moves to READY. Repeating the call for an already ready line is a"
              + " no-op.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Line marked as ready",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Order or order line not found"),
        @ApiResponse(
            responseCode = "409",
            description = "Order is not in PREPARING (or READY for an already-ready line) status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}/details/{detailId}/ready")
  @JustKitchenOrAdmin
  public ResponseEntity<OrderResponse> markOrderDetailAsReady(
      @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id,
      @Parameter(description = "Order detail ID", example = "10", required = true) @PathVariable
          Long detailId) {
    Order order = markAsReadyUseCase.markDetailAsReady(id, detailId);
    OrderResponse response = orderResponseMapper.toResponse(order);
    if (order.getStatus() == OrderStatus.READY) {
      orderNotificationService.notifyOrderReady(response);
    }
    return ResponseEntity.ok(response);
  }

  /**
   * Delivers an order.
   *
   * @param id the order ID
   * @return the delivered order
   */
  @Operation(
      tags = {"Orders"},
      summary = "Deliver order",
      description =
          "Marks an order as DELIVERED and releases the table. "
              + "Changes status from READY to DELIVERED.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Order delivered successfully",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "409", description = "Order is not in READY status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}/deliver")
  @JustOperationsStaff
  public ResponseEntity<OrderResponse> deliverOrder(
      @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id) {
    Order order = deliveryUseCase.markAsDelivered(id);
    OrderResponse response = orderResponseMapper.toResponse(order);
    orderNotificationService.notifyOrderDelivered(response);
    if (order.getTable() != null) {
      tableNotificationService.notifyTableStatusChanged(TableResponse.fromDomain(order.getTable()));
    }
    return ResponseEntity.ok(response);
  }

  /**
   * Returns a single order with its details, options and totals.
   *
   * @param id the order id
   * @return the order response
   */
  @Operation(
      tags = {"Orders"},
      summary = "Get order by id",
      description = "Retrieves a single order with its details, options and totals.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Order retrieved successfully",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  @JustOperationsStaff
  public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(orderResponseMapper.toResponse(orderQueryUseCase.findById(id)));
  }

  /**
   * Queries orders with optional filters and pagination.
   *
   * @param status the single order status filter (backward compat)
   * @param statuses comma-separated order statuses filter
   * @param search optional product/option name filter (partial, case-insensitive)
   * @param areaId optional preparation area (station) filter
   * @param startDate the start date filter
   * @param endDate the end date filter
   * @param page the page number (0-based)
   * @param size the page size
   * @param sort the sort field and direction
   * @return the paginated list of orders
   */
  @Operation(
      tags = {"Orders"},
      summary = "Query orders",
      description =
          "Retrieves orders with optional filters by status, product/option name, and date range, "
              + "with pagination.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Orders retrieved successfully",
            content =
                @Content(
                    schema = @Schema(implementation = PageResponse.class),
                    mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid status value"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  @JustOperationsStaff
  public ResponseEntity<PageResponse<OrderResponse>> getOrders(
      @Parameter(
              description = "Order status filter (QUEUE, PREPARING, READY, DELIVERED, CANCELLED)",
              example = "QUEUE")
          @RequestParam(required = false)
          String status,
      @Parameter(
              description =
                  "Comma-separated order statuses filter"
                      + " (QUEUE, PREPARING, READY, DELIVERED, CANCELLED)",
              example = "QUEUE,READY")
          @RequestParam(required = false)
          String statuses,
      @Parameter(
              description = "Optional product/option name filter (partial, case-insensitive)",
              example = "burger")
          @RequestParam(required = false)
          String search,
      @Parameter(
              description =
                  "Optional preparation area (station) filter. Returns orders with at least one"
                      + " line prepared in that area.",
              example = "1")
          @RequestParam(required = false)
          Long areaId,
      @Parameter(
              description = "Start date for filtering (ISO DateTime)",
              example = "2026-01-01T00:00:00")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime startDate,
      @Parameter(
              description = "End date for filtering (ISO DateTime)",
              example = "2026-12-31T23:59:59")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime endDate,
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20")
          int size,
      @Parameter(description = "Sort field and direction (e.g. date,desc)", example = "date,desc")
          @RequestParam(defaultValue = "date,desc")
          String sort) {
    List<OrderStatus> orderStatuses = new ArrayList<>();
    if (statuses != null && !statuses.isBlank()) {
      for (String s : statuses.split(",")) {
        orderStatuses.add(OrderStatus.valueOf(s.trim().toUpperCase()));
      }
    } else if (status != null && !status.isBlank()) {
      orderStatuses.add(OrderStatus.valueOf(status.toUpperCase()));
    }

    OrderQueryResult result;
    if (search != null && !search.isBlank()) {
      result =
          orderQueryUseCase.findOrdersPage(
              orderStatuses, search, startDate, endDate, areaId, page, size, sort);
    } else {
      result =
          orderQueryUseCase.findOrdersPage(
              orderStatuses, startDate, endDate, areaId, page, size, sort);
    }

    List<OrderResponse> responses = orderResponseMapper.toResponses(result.items());
    return ResponseEntity.ok(
        PageResponse.of(responses, result.total(), result.page(), result.size()));
  }
}
