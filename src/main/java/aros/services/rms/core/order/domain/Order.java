package aros.services.rms.core.order.domain;

import aros.services.rms.core.product.domain.Product;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Order {
    private Long id;
    private Long tableId;
    private String status; // PENDING, PREPARING, DELIVERED, etc.
    
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Comportamiento: Agrega un producto al pedido actual.
     */
    public void addItem(Product product, Integer quantity, String note) {
        if (!product.isAvailable()) {
            // Nota: En la Fase 3 cambiaremos esto por una excepción de dominio (ej. ProductNotAvailableException)
            throw new IllegalArgumentException("El producto no está disponible o no tiene stock suficiente.");
        }
        
        OrderItem newItem = OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .note(note)
                .build();
                
        this.items.add(newItem);
    }

    /**
     * Comportamiento: Retorna el total del pedido.
     */
    public BigDecimal calculateTotal() {
        return items.stream()
                .map(OrderItem::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Comportamiento: Retorna el resumen inmutable para mostrar antes de confirmar.
     */
    public List<OrderItem> getSummary() {
        return List.copyOf(items);
    }
}