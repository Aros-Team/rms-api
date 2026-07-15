/* (C) 2026 */

package aros.services.rms.infraestructure.order.api;

import aros.services.rms.core.order.application.dto.TakeOrderCommand;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.DeliveryUseCase;
import aros.services.rms.core.order.port.input.MarkAsReadyUseCase;
import aros.services.rms.core.order.port.input.OrderQueryUseCase;
import aros.services.rms.core.order.port.input.PreparationUseCase;
import aros.services.rms.core.order.port.input.TakeOrderUseCase;
import aros.services.rms.core.order.port.input.UpdateOrderUseCase;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse;
import aros.services.rms.infraestructure.order.api.dto.OrderResponseMapper;
import aros.services.rms.infraestructure.order.api.dto.TakeOrderRequest;
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
      description = "Creates a new order with status EN_COLA. Requires an available table.",
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
  public ResponseEntity<OrderResponse> takeOrder(@Valid @RequestBody TakeOrderRequest request) {
    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(request.tableId())
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
      description = "Cancels an existing order that is in EN_COLA status.",
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
      description = "Updates the details of an order in EN_COLA status.",
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
  public ResponseEntity<OrderResponse> updateOrder(
      @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id,
      @Valid @RequestBody TakeOrderRequest request) {
    TakeOrderCommand command =
        TakeOrderCommand.builder()
            .tableId(request.tableId())
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
      description =
          "Takes the oldest order from the queue and changes its status to EN_PREPARACION.",
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
  public ResponseEntity<OrderResponse> prepareNextOrder() {
    Order order = preparationUseCase.processNextOrder();
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
          "Marks a specific order as LISTA for delivery."
              + " Changes status from EN_PREPARACION to LISTA.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Order marked as ready",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "409", description = "Order is not in EN_PREPARACION status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}/ready")
  public ResponseEntity<OrderResponse> markOrderAsReady(
      @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id) {
    Order order = markAsReadyUseCase.markAsReady(id);
    OrderResponse response = orderResponseMapper.toResponse(order);
    orderNotificationService.notifyOrderReady(response);
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
          "Marks an order as ENTREGADA and releases the table. "
              + "Changes status from LISTA to ENTREGADA.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Order delivered successfully",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "409", description = "Order is not in LISTA status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}/deliver")
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
   * Queries orders with optional filters.
   *
   * @param status the order status filter
   * @param startDate the start date filter
   * @param endDate the end date filter
   * @return the list of orders
   */
  @Operation(
      tags = {"Orders"},
      summary = "Query orders",
      description = "Retrieves orders with optional filters by status and date range.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Orders retrieved successfully",
            content =
                @Content(
                    schema = @Schema(implementation = OrderResponse.class),
                    mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid status value"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<List<OrderResponse>> getOrders(
      @Parameter(
              description =
                  "Order status filter (EN_COLA, EN_PREPARACION, LISTA, ENTREGADA, CANCELADA)",
              example = "EN_COLA")
          @RequestParam(required = false)
          String status,
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
          LocalDateTime endDate) {
    OrderStatus orderStatus = null;
    if (status != null && !status.isBlank()) {
      orderStatus = OrderStatus.valueOf(status.toUpperCase());
    }

    List<Order> orders = orderQueryUseCase.findOrders(orderStatus, startDate, endDate);
    List<OrderResponse> responses =
        orders.stream().map(orderResponseMapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }
}
