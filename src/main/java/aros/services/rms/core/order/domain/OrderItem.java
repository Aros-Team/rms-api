package aros.services.rms.core.order.domain;

import aros.services.rms.core.product.domain.Product;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrderItem {
    private Product product;
    private Integer quantity;
    private String note; // Ej: "sin picante", "término medio"

    public BigDecimal calculateSubtotal() {
        if (product == null || product.getPrice() == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}