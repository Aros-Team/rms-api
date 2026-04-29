/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.ProductRecipeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for product recipes. */
@Repository
public interface ProductRecipeRepository extends JpaRepository<ProductRecipeEntity, Long> {

  /**
   * Finds product recipes by product ID.
   *
   * @param productId the product ID
   * @return the list of product recipe entities
   */
  List<ProductRecipeEntity> findByProductId(Long productId);
}
