package aros.services.rms.infraestructure.specialselection.persistence.jpa;

import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionQuestionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for special selection questions. */
@Repository
public interface SpecialSelectionQuestionRepository
    extends JpaRepository<SpecialSelectionQuestionEntity, Long> {
  /**
   * Finds questions associated with the given product.
   *
   * @param productId the product identifier
   * @return list of question entities for the product
   */
  List<SpecialSelectionQuestionEntity> findByProductId(Long productId);

  /**
   * Deletes questions associated with the given product.
   *
   * @param productId the product identifier
   */
  void deleteByProductId(Long productId);
}
