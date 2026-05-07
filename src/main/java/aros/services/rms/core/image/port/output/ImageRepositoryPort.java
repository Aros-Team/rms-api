package aros.services.rms.core.image.port.output;

import aros.services.rms.core.image.domain.ProductImage;
import java.util.List;
import java.util.Optional;

/** Output port for product image persistence. */
public interface ImageRepositoryPort {
  /** Saves or updates a product image. */
  ProductImage save(ProductImage productImage);

  /** Finds image by ID. Returns empty if not found. */
  Optional<ProductImage> findById(Long id);

  /** Finds all images for a product. */
  List<ProductImage> findByProductId(Long productId);

  /** Deletes image by ID. */
  void deleteById(Long id);
}
