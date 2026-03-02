package aros.services.rms.core.product.domain;

import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long id);
}