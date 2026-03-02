package aros.services.rms.core.product.port.output;

import aros.services.rms.core.product.domain.Product;
import java.util.Optional;

public interface ProductRepositoryPort {
    Optional<Product> findById(Long id);
}