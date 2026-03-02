package aros.services.rms.core.order.domain;

import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
    private Long id;
    private Product product;
    private Double unitPrice;
    private String instructions;
    private List<ProductOption> selectedOptions;
}