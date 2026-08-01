/* (C) 2026 */

package aros.services.rms.infraestructure.category.persistence.jpa;

import aros.services.rms.infraestructure.category.persistence.OptionCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for OptionCategory entity persistence operations. */
@Repository
public interface OptionCategoryRepository extends JpaRepository<OptionCategory, Long> {

  /** Finds option categories whose name contains the given string (case-insensitive). */
  List<OptionCategory> findByNameContainingIgnoreCase(String name);
}
