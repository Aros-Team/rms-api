/* (C) 2026 */
package aros.services.rms.infraestructure.product.persistence.jpa;

import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Persistence adapter that implements ProductOptionRepositoryPort using JPA. */
@Component
@RequiredArgsConstructor
public class ProductOptionPersistenceAdapter implements ProductOptionRepositoryPort {

  private final ProductOptionRepository productOptionRepository;
  private final ProductMapper productMapper;

  @Override
  public ProductOption save(ProductOption productOption) {
    aros.services.rms.infraestructure.product.persistence.ProductOption entity =
        productMapper.toProductOptionEntity(productOption);
    aros.services.rms.infraestructure.product.persistence.ProductOption savedEntity =
        productOptionRepository.save(entity);
    return productMapper.toProductOptionDomain(savedEntity);
  }

  @Override
  public Optional<ProductOption> findById(Long id) {
    return productOptionRepository.findById(id).map(productMapper::toProductOptionDomain);
  }

  @Override
  public List<ProductOption> findAllById(List<Long> ids) {
    return productOptionRepository.findAllById(ids).stream()
        .map(productMapper::toProductOptionDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<ProductOption> findAll() {
    return productOptionRepository.findAll().stream()
        .map(productMapper::toProductOptionDomain)
        .collect(Collectors.toList());
  }
}
