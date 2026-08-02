/* (C) 2026 */

package aros.services.rms.infraestructure.category.persistence.jpa;

import aros.services.rms.infraestructure.category.persistence.OptionGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for OptionGroup entity persistence operations. */
@Repository
public interface OptionGroupRepository extends JpaRepository<OptionGroup, Long> {

  /** Finds option categories whose name contains the given string (case-insensitive). */
  List<OptionGroup> findByNameContainingIgnoreCase(String name);
}
