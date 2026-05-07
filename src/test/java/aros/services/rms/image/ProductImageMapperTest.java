/* (C) 2026 */

package aros.services.rms.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import aros.services.rms.core.image.domain.ProductImage;
import aros.services.rms.infraestructure.image.persistence.jpa.ProductImageEntity;
import aros.services.rms.infraestructure.image.persistence.jpa.ProductImageMapper;
import aros.services.rms.infraestructure.product.persistence.Product;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductImageMapperTest {

  private final ProductImageMapper mapper = new ProductImageMapper();

  @Test
  void shouldConvertDomainToEntity() {
    Instant now = Instant.now();
    ProductImage domain =
        ProductImage.builder()
            .id(1L)
            .productId(42L)
            .originalFilename("test.jpg")
            .contentType("image/jpeg")
            .originalSizeBytes(1024L)
            .storageKey("products/42/uuid")
            .createdAt(now)
            .build();

    ProductImageEntity entity = mapper.toEntity(domain);

    assertNotNull(entity);
    assertEquals(1L, entity.getId());
    assertEquals(42L, entity.getProduct().getId());
    assertEquals("test.jpg", entity.getOriginalFilename());
    assertEquals("image/jpeg", entity.getContentType());
    assertEquals(1024L, entity.getOriginalSizeBytes());
    assertEquals("products/42/uuid", entity.getStorageKey());
    assertEquals(now, entity.getCreatedAt());
  }

  @Test
  void shouldConvertEntityToDomain() {
    Instant now = Instant.now();
    ProductImageEntity entity =
        ProductImageEntity.builder()
            .id(1L)
            .product(Product.builder().id(42L).build())
            .originalFilename("test.jpg")
            .contentType("image/jpeg")
            .originalSizeBytes(1024L)
            .storageKey("products/42/uuid")
            .createdAt(now)
            .build();

    ProductImage domain = mapper.toDomain(entity);

    assertNotNull(domain);
    assertEquals(1L, domain.getId());
    assertEquals(42L, domain.getProductId());
    assertEquals("test.jpg", domain.getOriginalFilename());
    assertEquals("image/jpeg", domain.getContentType());
    assertEquals(1024L, domain.getOriginalSizeBytes());
    assertEquals("products/42/uuid", domain.getStorageKey());
    assertEquals(now, domain.getCreatedAt());
  }

  @Test
  void shouldReturnNullWhenDomainIsNull() {
    ProductImageEntity entity = mapper.toEntity(null);
    assertNull(entity);
  }

  @Test
  void shouldReturnNullWhenEntityIsNull() {
    ProductImage domain = mapper.toDomain(null);
    assertNull(domain);
  }
}
