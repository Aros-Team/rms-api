package aros.services.rms.core.order.application.usecases;

import aros.services.rms.core.order.application.exception.OrderNotFoundException;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderRepository;
import aros.services.rms.core.order.port.input.AddProductToOrderInput;
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

        // 3. Aplicar lógica de negocio (el dominio valida stock y disponibilidad)
        order.addItem(product, quantity, note);

        // 4. Persistir los cambios (Puerto de salida)
        return orderRepository.save(order);
    }
}