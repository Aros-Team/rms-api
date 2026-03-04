package aros.services.rms.infraestructure.order.api;

import aros.services.rms.core.order.application.usecases.TakeOrderCommand;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.order.port.input.DeliveryUseCase;
import aros.services.rms.core.order.port.input.MarkAsReadyUseCase;
import aros.services.rms.core.order.port.input.OrderQueryUseCase;
import aros.services.rms.core.order.port.input.PreparationUseCase;
import aros.services.rms.core.order.port.input.TakeOrderUseCase;
import aros.services.rms.core.order.port.input.UpdateOrderUseCase;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse;
import aros.services.rms.infraestructure.order.api.dto.TakeOrderRequest;
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

/**
 * Controlador REST para gestión de órdenes.
 * Expone endpoints para todo el ciclo de vida de una orden.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final TakeOrderUseCase takeOrderUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final PreparationUseCase preparationUseCase;
    private final MarkAsReadyUseCase markAsReadyUseCase;
    private final DeliveryUseCase deliveryUseCase;
    private final OrderQueryUseCase orderQueryUseCase;

    /**
     * Crea una nueva orden. La orden inicia en estado QUEUE.
     * Requiere mesa disponible.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> takeOrder(@Valid @RequestBody TakeOrderRequest request) {
        TakeOrderCommand command = TakeOrderCommand.builder()
                .tableId(request.tableId())
                .details(request.details().stream()
                        .map(detail -> TakeOrderCommand.OrderDetailCommand.builder()
                                .productId(detail.productId())
                                .instructions(detail.instructions())
                                .selectedOptionIds(detail.selectedOptionIds())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        Order order = takeOrderUseCase.execute(command);
        return new ResponseEntity<>(OrderResponse.fromDomain(order), HttpStatus.CREATED);
    }

    /**
     * Cancela una orden existente.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        Order order = updateOrderUseCase.cancel(id);
        return ResponseEntity.ok(OrderResponse.fromDomain(order));
    }

    /**
     * Actualiza los detalles de una orden.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody TakeOrderRequest request
    ) {
        TakeOrderCommand command = TakeOrderCommand.builder()
                .tableId(request.tableId())
                .details(request.details().stream()
                        .map(detail -> TakeOrderCommand.OrderDetailCommand.builder()
                                .productId(detail.productId())
                                .instructions(detail.instructions())
                                .selectedOptionIds(detail.selectedOptionIds())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        Order order = updateOrderUseCase.update(id, command);
        return ResponseEntity.ok(OrderResponse.fromDomain(order));
    }

    /**
     * Toma la siguiente orden de la cola y la pone en preparación.
     * Cambia estado de QUEUE a PREPARING.
     */
    @PutMapping("/prepare")
    public ResponseEntity<OrderResponse> prepareNextOrder() {
        Order order = preparationUseCase.processNextOrder();
        return ResponseEntity.ok(OrderResponse.fromDomain(order));
    }

    /**
     * Marca una orden específica como lista para entregar.
     * Cambia estado de PREPARING a READY.
     */
    @PutMapping("/{id}/ready")
    public ResponseEntity<OrderResponse> markOrderAsReady(@PathVariable Long id) {
        Order order = markAsReadyUseCase.markAsReady(id);
        return ResponseEntity.ok(OrderResponse.fromDomain(order));
    }

    /**
     * Entrega una orden al cliente.
     * Cambia estado de READY a DELIVERED y libera la mesa.
     */
    @PutMapping("/{id}/deliver")
    public ResponseEntity<OrderResponse> deliverOrder(@PathVariable Long id) {
        Order order = deliveryUseCase.markAsDelivered(id);
        return ResponseEntity.ok(OrderResponse.fromDomain(order));
    }

    /**
     * Consulta órdenes con filtros opcionales por estado y rango de fechas.
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        OrderStatus orderStatus = null;
        if (status != null && !status.isBlank()) {
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        }

        List<Order> orders = orderQueryUseCase.findOrders(orderStatus, startDate, endDate);
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}