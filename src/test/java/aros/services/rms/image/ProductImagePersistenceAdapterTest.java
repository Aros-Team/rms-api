/* (C) 2026 */

package aros.services.rms.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.image.domain.ProductImage;
import aros.services.rms.infraestructure.image.persistence.jpa.ProductImageEntity;
import aros.services.rms.infraestructure.image.persistence.jpa.ProductImageMapper;
import aros.services.rms.infraestructure.image.persistence.jpa.ProductImagePersistenceAdapter;
import aros.services.rms.infraestructure.image.persistence.jpa.ProductImageRepository;
import aros.services.rms.infraestructure.product.persistence.Product;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductImagePersistenceAdapterTest {

  @Mock private ProductImageRepository productImageRepository;

  @Mock private ProductImageMapper productImageMapper;

  @InjectMocks private ProductImagePersistenceAdapter adapter;

  @Test
  void shouldSaveProductImage() {
    Instant now = Instant.now();
    ProductImage domain =
        ProductImage.builder()
            .productId(42L)
            .originalFilename("test.jpg")
            .contentType("image/jpeg")
            .originalSizeBytes(1024L)
            .storageKey("products/42/uuid")
            .createdAt(now)
            .build();

    ProductImageEntity entity =
        ProductImageEntity.builder()
            .product(Product.builder().id(42L).build())
            .originalFilename("test.jpg")
            .contentType("image/jpeg")
            .originalSizeBytes(1024L)
            .storageKey("products/42/uuid")
            .createdAt(now)
            .build();

    ProductImageEntity savedEntity =
        ProductImageEntity.builder()
            .id(1L)
            .product(Product.builder().id(42L).build())
            .originalFilename("test.jpg")
            .contentType("image/jpeg")
            .originalSizeBytes(1024L)
            .storageKey("products/42/uuid")
            .createdAt(now)
            .build();

    ProductImage savedDomain =
        ProductImage.builder()
            .id(1L)
            .productId(42L)
            .originalFilename("test.jpg")
            .contentType("image/jpeg")
            .originalSizeBytes(1024L)
            .storageKey("products/42/uuid")
            .createdAt(now)
            .build();

    when(productImageMapper.toEntity(domain)).thenReturn(entity);
    when(productImageRepository.save(entity)).thenReturn(savedEntity);
    when(productImageMapper.toDomain(savedEntity)).thenReturn(savedDomain);

    ProductImage result = adapter.save(domain);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals(42L, result.getProductId());
    verify(productImageRepository).save(entity);
  }

  @Test
  void shouldFindById() {
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

    when(productImageRepository.findById(1L)).thenReturn(Optional.of(entity));
    when(productImageMapper.toDomain(entity)).thenReturn(domain);

    Optional<ProductImage> result = adapter.findById(1L);

    assertTrue(result.isPresent());
    assertEquals(1L, result.get().getId());
    verify(productImageRepository).findById(1L);
  }

  @Test
  void shouldReturnEmptyOptionalWhenNotFound() {
    when(productImageRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<ProductImage> result = adapter.findById(999L);

    assertTrue(result.isEmpty());
    verify(productImageRepository).findById(999L);
  }

  @Test
  void shouldFindByProductId() {
    Instant now = Instant.now();
    ProductImageEntity entity1 =
        ProductImageEntity.builder()
            .id(1L)
            .product(Product.builder().id(42L).build())
            .originalFilename("test1.jpg")
            .contentType("image/jpeg")
            .originalSizeBytes(1024L)
            .storageKey("products/42/uuid1")
            .createdAt(now)
            .build();

    ProductImageEntity entity2 =
        ProductImageEntity.builder()
            .id(2L)
            .product(Product.builder().id(42L).build())
            .originalFilename("test2.jpg")
            .contentType("image/png")
            .originalSizeBytes(2048L)
            .storageKey("products/42/uuid2")
            .createdAt(now)
            .build();

    ProductImage domain1 =
        ProductImage.builder()
            .id(1L)
            .productId(42L)
            .originalFilename("test1.jpg")
            .contentType("image/jpeg")
            .originalSizeBytes(1024L)
            .storageKey("products/42/uuid1")
            .createdAt(now)
            .build();

    ProductImage domain2 =
        ProductImage.builder()
            .id(2L)
            .productId(42L)
            .originalFilename("test2.jpg")
            .contentType("image/png")
            .originalSizeBytes(2048L)
            .storageKey("products/42/uuid2")
            .createdAt(now)
            .build();

    when(productImageRepository.findByProductId(42L)).thenReturn(List.of(entity1, entity2));
    when(productImageMapper.toDomain(entity1)).thenReturn(domain1);
    when(productImageMapper.toDomain(entity2)).thenReturn(domain2);

    List<ProductImage> result = adapter.findByProductId(42L);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(1L, result.get(0).getId());
    assertEquals(2L, result.get(1).getId());
    verify(productImageRepository).findByProductId(42L);
  }

  @Test
  void shouldDeleteById() {
    doNothing().when(productImageRepository).deleteById(1L);

    adapter.deleteById(1L);

    verify(productImageRepository).deleteById(1L);
  }
}
