package aros.services.rms.infraestructure.order.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TakeOrderRequest(
        @NotNull(message = "Table ID is required")
        Long tableId,

        @NotEmpty(message = "Order details cannot be empty")
        @Valid
        List<OrderDetailRequest> details
) {
    public record OrderDetailRequest(
            @NotNull(message = "Product ID is required")
            Long productId,
            String instructions,
            List<Long> selectedOptionIds
    ) {}
}