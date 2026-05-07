/* (C) 2026 */

package aros.services.rms.infraestructure.image.persistence.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for ProductImage entity persistence operations. */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {

  /** Finds all images for a specific product. */
  List<ProductImageEntity> findByProductId(Long productId);

  /** Deletes all images for a specific product. */
  void deleteByProductId(Long productId);
}
