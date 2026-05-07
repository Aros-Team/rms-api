/* (C) 2026 */

package aros.services.rms.infraestructure.image.persistence.jpa;

import aros.services.rms.core.image.domain.ProductImage;
import aros.services.rms.core.image.port.output.ImageRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Persistence adapter that implements ImageRepositoryPort using JPA. */
@Component
@RequiredArgsConstructor
public class ProductImagePersistenceAdapter implements ImageRepositoryPort {

  private final ProductImageRepository productImageRepository;
  private final ProductImageMapper productImageMapper;

  @Override
  public ProductImage save(ProductImage productImage) {
    ProductImageEntity entity = productImageMapper.toEntity(productImage);
    ProductImageEntity savedEntity = productImageRepository.save(entity);
    return productImageMapper.toDomain(savedEntity);
  }

  @Override
  public Optional<ProductImage> findById(Long id) {
    return productImageRepository.findById(id).map(productImageMapper::toDomain);
  }

  @Override
  public List<ProductImage> findByProductId(Long productId) {
    return productImageRepository.findByProductId(productId).stream()
        .map(productImageMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteById(Long id) {
    productImageRepository.deleteById(id);
  }
}
