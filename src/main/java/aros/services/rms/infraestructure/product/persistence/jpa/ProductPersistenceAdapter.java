/* (C) 2026 */

package aros.services.rms.infraestructure.product.persistence.jpa;

import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  @Override
  public List<Product> findAllById(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return productRepository.findAllById(ids).stream()
        .map(productMapper::toProductDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Page<Product> findAllActive(Pageable pageable) {
    return productRepository.findAllActive(true, pageable).map(productMapper::toProductDomain);
  }

  @Override
  public List<Product> findAllStandard() {
    return productRepository.findAllStandard().stream()
        .map(productMapper::toProductDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Page<Product> findAllStandard(Pageable pageable) {
    return productRepository.findAllStandard(pageable).map(productMapper::toProductDomain);
  }

  @Override
  public List<Product> findByCategoryIdsStandard(List<Long> categoryIds) {
    return productRepository.findByCategoryIdInStandard(categoryIds).stream()
        .map(productMapper::toProductDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Page<Product> searchByNameOrDescriptionOrCategoryName(
      String search,
      List<Long> categoryIds,
      boolean includeInactive,
      boolean includeSelections,
      Pageable pageable) {
    List<Long> effectiveCategoryIds =
        (categoryIds == null || categoryIds.isEmpty()) ? null : categoryIds;
    return productRepository
        .searchByNameOrDescriptionOrCategoryName(
            search, effectiveCategoryIds, includeInactive, includeSelections, pageable)
        .map(productMapper::toProductDomain);
  }
}
