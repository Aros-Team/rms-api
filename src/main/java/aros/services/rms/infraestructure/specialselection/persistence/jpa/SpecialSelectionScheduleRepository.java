package aros.services.rms.infraestructure.specialselection.persistence.jpa;

import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionScheduleEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for special selection schedule entries. */
@Repository
public interface SpecialSelectionScheduleRepository
    extends JpaRepository<SpecialSelectionScheduleEntity, Long> {
  /**
   * Finds schedule entries associated with the given product.
   *
   * @param productId the product identifier
   * @return list of schedule entities for the product
   */
  List<SpecialSelectionScheduleEntity> findByProductId(Long productId);

  /**
   * Deletes schedule entries associated with the given product.
   *
   * @param productId the product identifier
   */
  void deleteByProductId(Long productId);
}
