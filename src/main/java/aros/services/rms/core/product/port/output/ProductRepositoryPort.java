/* (C) 2026 */
package aros.services.rms.core.product.port.output;

import aros.services.rms.core.product.domain.Product;
import java.util.List;
import java.util.Optional;

/** Output port for product persistence operations. */
public interface ProductRepositoryPort {
  Product save(Product product);

  Optional<Product> findById(Long id);

  List<Product> findAll();

  List<Product> findByCategoryIds(List<Long> categoryIds);

  boolean existsById(Long id);
}
