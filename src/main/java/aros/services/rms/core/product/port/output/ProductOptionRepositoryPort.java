/* (C) 2026 */
package aros.services.rms.core.product.port.output;

import aros.services.rms.core.product.domain.ProductOption;
import java.util.List;
import java.util.Optional;

/**
 * Output port for product option persistence operations.
 */
public interface ProductOptionRepositoryPort {
  ProductOption save(ProductOption productOption);

  Optional<ProductOption> findById(Long id);

  List<ProductOption> findAllById(List<Long> ids);

  List<ProductOption> findAll();
}
