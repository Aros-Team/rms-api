/* (C) 2026 */

package aros.services.rms.infraestructure.product.persistence.jpa;

import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Persistence adapter that implements ProductRepositoryPort using JPA. */
@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepositoryPort {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  @Override
  public Product save(Product product) {
    aros.services.rms.infraestructure.product.persistence.Product entity =
        productMapper.toProductEntity(product);
    aros.services.rms.infraestructure.product.persistence.Product savedEntity =
        productRepository.save(entity);
    return productMapper.toProductDomain(savedEntity);
  }

  @Override
  public Optional<Product> findById(Long id) {
    return productRepository.findById(id).map(productMapper::toProductDomain);
  }

  @Override
  public List<Product> findAll() {
    return productRepository.findAll().stream()
        .map(productMapper::toProductDomain)
        .collect(Collectors.toList());
  }

  @Override
  public boolean existsById(Long id) {
    return productRepository.existsById(id);
  }

  @Override
  public List<Product> findByCategoryIds(List<Long> categoryIds) {
    return productRepository.findByCategoryIdIn(categoryIds).stream()
        .map(productMapper::toProductDomain)
        .collect(Collectors.toList());
  }
}
