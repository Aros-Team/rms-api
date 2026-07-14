package aros.services.rms.infraestructure.specialselection.persistence.jpa;

import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionGroupEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for special selection option groups. */
@Repository
public interface SpecialSelectionGroupRepository
    extends JpaRepository<SpecialSelectionGroupEntity, Long> {
  /**
   * Finds option groups associated with the given product.
   *
   * @param productId the product identifier
   * @return list of option groups for the product
   */
  List<SpecialSelectionGroupEntity> findByProductId(Long productId);

  /**
   * Deletes option groups associated with the given product.
   *
   * @param productId the product identifier
   */
  void deleteByProductId(Long productId);
}
