package aros.services.rms.core.product.domain;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class Product {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private boolean isActive;

    /**
     * Regla de negocio: Un producto solo está disponible si está activo y su stock es mayor a 0.
     */
    public boolean isAvailable() {
        return isActive && stock != null && stock > 0;
    }
}