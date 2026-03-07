/* (C) 2026 */
package aros.services.rms.infraestructure.product.persistence.jpa;

import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter
    implements ProductRepositoryPort, ProductOptionRepositoryPort {

  private final ProductRepository productRepository;
  private final ProductOptionRepository productOptionRepository;
  private final ProductMapper productMapper;

  @Override
  public Optional<Product> findById(Long id) {
    return productRepository.findById(id).map(productMapper::toProductDomain);
  }

  @Override
  public List<ProductOption> findAllById(List<Long> ids) {
    return productOptionRepository.findAllById(ids).stream()
        .map(productMapper::toProductOptionDomain)
        .collect(Collectors.toList());
  }
}
