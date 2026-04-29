/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.OptionRecipeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for option recipes. */
@Repository
public interface OptionRecipeRepository extends JpaRepository<OptionRecipeEntity, Long> {

  /**
   * Finds option recipes by option IDs.
   *
   * @param optionIds the list of option IDs
   * @return the list of option recipe entities
   */
  List<OptionRecipeEntity> findByOptionIdIn(List<Long> optionIds);
}
