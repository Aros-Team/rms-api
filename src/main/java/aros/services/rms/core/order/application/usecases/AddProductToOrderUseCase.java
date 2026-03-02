package aros.services.rms.core.order.application.usecases;

import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderRepository;
import aros.services.rms.core.order.port.input.AddProductToOrderInput;
import aros.services.rms.core.product.application.exception.ProductNotAvailableException;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;

/**
 * Orquesta la lógica para agregar un producto a un pedido.
 * Implementa el puerto de entrada (Input Port).
 */
@RequiredArgsConstructor
public class AddProductToOrderUseCase implements AddProductToOrderInput {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    public Order execute(Long orderId, Long productId, Integer quantity, String note) {
        // 1. Buscar el pedido existente
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 2. Buscar el producto en el catálogo (módulo product)
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // 3. Validar disponibilidad (Regla de negocio: stock > 0)
        if (!product.isAvailable()) {
            throw new ProductNotAvailableException(productId);
        }

        // 4. Aplicar lógica de negocio
        order.addItem(product, quantity, note);

        // 5. Persistir los cambios (Puerto de salida)
        return orderRepository.save(order);
    }
}
