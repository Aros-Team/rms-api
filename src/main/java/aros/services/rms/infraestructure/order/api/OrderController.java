package aros.services.rms.infraestructure.order.api;

import aros.services.rms.core.order.application.usecases.TakeOrderCommand;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.port.input.TakeOrderUseCase;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse;
import aros.services.rms.infraestructure.order.api.dto.TakeOrderRequest;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final TakeOrderUseCase takeOrderUseCase;

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
}