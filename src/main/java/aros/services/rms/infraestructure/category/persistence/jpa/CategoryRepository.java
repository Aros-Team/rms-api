/* (C) 2026 */
package aros.services.rms.infraestructure.category.persistence.jpa;

import aros.services.rms.infraestructure.category.persistence.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for Category entity persistence operations. */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  /** Finds a category by its name. */
  Optional<Category> findByName(String name);
}
