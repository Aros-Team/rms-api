package aros.services.rms.infraestructure.order.api;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.port.input.AddProductToOrderInput;
import aros.services.rms.infraestructure.order.api.dto.AddProductToOrderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    // Inyectamos el PUERTO (la interfaz), no la implementación. 
    // Esto mantiene la arquitectura limpia.
    private final AddProductToOrderInput addProductToOrderInput;

    /**
     * Endpoint para que el mesero agregue productos a un pedido.
     * Ejemplo de ruta: POST /api/v1/orders/5/items
     */
    @PostMapping("/{orderId}/items")
    public ResponseEntity<Order> addProductToOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody AddProductToOrderRequest request) {

        // Llamamos al caso de uso de nuestro Core
        Order updatedOrder = addProductToOrderInput.execute(
                orderId,
                request.getProductId(),
                request.getQuantity(),
                request.getNote()
        );

        // Retornamos el pedido actualizado. 
        // El frontend podrá usar order.getSummary() y order.calculateTotal() 
        // para mostrarle el resumen al mesero (Criterio 3 de tu historia).
        return ResponseEntity.ok(updatedOrder);
    }
}