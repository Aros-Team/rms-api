package aros.services.rms.infraestructure.specialselection.persistence.jpa;

import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionAdditionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for special selection additions. */
@Repository
public interface SpecialSelectionAdditionRepository
    extends JpaRepository<SpecialSelectionAdditionEntity, Long> {
  /**
   * Finds additions associated with the given product.
   *
   * @param productId the product identifier
   * @return list of additions for the product
   */
  List<SpecialSelectionAdditionEntity> findByProductId(Long productId);

  /**
   * Deletes additions associated with the given product.
   *
   * @param productId the product identifier
   */
  void deleteByProductId(Long productId);
}
