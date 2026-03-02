package aros.services.rms.core.product.domain;

import aros.services.rms.core.category.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private Double basePrice;
    private boolean hasOptions;
    private Category category;
}