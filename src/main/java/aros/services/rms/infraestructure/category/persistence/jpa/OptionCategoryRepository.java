/* (C) 2026 */
package aros.services.rms.infraestructure.category.persistence.jpa;

import aros.services.rms.infraestructure.category.persistence.OptionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for OptionCategory entity persistence operations. */
@Repository
public interface OptionCategoryRepository extends JpaRepository<OptionCategory, Long> {}
