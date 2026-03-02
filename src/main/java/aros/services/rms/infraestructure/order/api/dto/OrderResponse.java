package aros.services.rms.infraestructure.order.api.dto;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.product.domain.ProductOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record OrderResponse(
        Long id,
        LocalDateTime date,
        Long tableId,
        List<OrderDetailResponse> details
) {
    public record OrderDetailResponse(
            Long id,
            Long productId,
            String productName,
            Double unitPrice,
            String instructions,
            List<ProductOptionResponse> selectedOptions
    ) {}

    public record ProductOptionResponse(
            Long id,
            String name,
            String type
    ) {}

    public static OrderResponse fromDomain(Order order) {
        if (order == null) return null;

        return new OrderResponse(
                order.getId(),
                order.getDate(),
                order.getTable() != null ? order.getTable().getId() : null,
                order.getDetails() != null ? order.getDetails().stream()
                        .map(OrderResponse::fromDomainDetail)
                        .collect(Collectors.toList()) : null
        );
    }

    private static OrderDetailResponse fromDomainDetail(OrderDetail detail) {
        if (detail == null) return null;

        return new OrderDetailResponse(
                detail.getId(),
                detail.getProduct() != null ? detail.getProduct().getId() : null,
                detail.getProduct() != null ? detail.getProduct().getName() : null,
                detail.getUnitPrice(),
                detail.getInstructions(),
                detail.getSelectedOptions() != null ? detail.getSelectedOptions().stream()
                        .map(OrderResponse::fromDomainOption)
                        .collect(Collectors.toList()) : null
        );
    }

    private static ProductOptionResponse fromDomainOption(ProductOption option) {
        if (option == null) return null;

        return new ProductOptionResponse(
                option.getId(),
                option.getName(),
                option.getType()
        );
    }
}