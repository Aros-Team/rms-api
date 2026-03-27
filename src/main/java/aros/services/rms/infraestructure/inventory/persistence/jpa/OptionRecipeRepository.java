/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.OptionRecipeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionRecipeRepository extends JpaRepository<OptionRecipeEntity, Long> {

  List<OptionRecipeEntity> findByOptionIdIn(List<Long> optionIds);
}
