/* (C) 2026 */

package aros.services.rms.infraestructure.image.persistence.jpa;

import aros.services.rms.core.image.domain.ProductImage;
import org.springframework.stereotype.Component;

/** Mapper between ProductImage domain model and ProductImage JPA entity. */
@Component
public class ProductImageMapper {

  /** Converts a domain ProductImage to a JPA entity. */
  public ProductImageEntity toEntity(ProductImage domain) {
    if (domain == null) {
      return null;
    }

    return ProductImageEntity.builder()
        .id(domain.getId())
        .product(
            domain.getProductId() != null
                ? aros.services.rms.infraestructure.product.persistence.Product.builder()
                    .id(domain.getProductId())
                    .build()
                : null)
        .originalFilename(domain.getOriginalFilename())
        .contentType(domain.getContentType())
        .originalSizeBytes(domain.getOriginalSizeBytes())
        .storageKey(domain.getStorageKey())
        .createdAt(domain.getCreatedAt())
        .build();
  }

  /** Converts a JPA entity ProductImage to a domain model. */
  public ProductImage toDomain(ProductImageEntity entity) {
    if (entity == null) {
      return null;
    }

    return ProductImage.builder()
        .id(entity.getId())
        .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
        .originalFilename(entity.getOriginalFilename())
        .contentType(entity.getContentType())
        .originalSizeBytes(entity.getOriginalSizeBytes())
        .storageKey(entity.getStorageKey())
        .createdAt(entity.getCreatedAt())
        .build();
  }
}
