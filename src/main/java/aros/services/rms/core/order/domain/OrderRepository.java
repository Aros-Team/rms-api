package aros.services.rms.core.order.domain;

import java.util.Optional;

import aros.services.rms.core.product.domain.Product;

// El core dicta las reglas: "Si quieres guardar mis pedidos, debes cumplir este contrato"
public interface OrderRepository {
    Optional<Order> findById(Long id);
    Order save(Order order);
    
    // También necesitaremos acceder a los productos para agregarlos
    Optional<Product> findProductById(Long productId);
}