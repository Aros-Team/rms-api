package aros.services.rms.infraestructure.specialselection.persistence.jpa;

import aros.services.rms.infraestructure.specialselection.persistence.ProductProductOptionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for product-product-option associations. */
@Repository
public interface ProductProductOptionRepository
    extends JpaRepository<
        ProductProductOptionEntity, ProductProductOptionEntity.ProductProductOptionId> {
  /**
   * Finds the associations belonging to the given selection group.
   *
   * @param selectionGroupId the selection group identifier
   * @return list of associations for the group
   */
  List<ProductProductOptionEntity> findBySelectionGroupId(Long selectionGroupId);

  /**
   * Deletes the associations belonging to the given selection group.
   *
   * @param selectionGroupId the selection group identifier
   */
  void deleteBySelectionGroupId(Long selectionGroupId);

  /**
   * Finds the associations belonging to the given product.
   *
   * @param productId the product identifier
   * @return list of associations for the product
   */
  List<ProductProductOptionEntity> findByProductId(Long productId);

  /**
   * Deletes the associations belonging to the given product.
   *
   * @param productId the product identifier
   */
  void deleteByProductId(Long productId);
}
