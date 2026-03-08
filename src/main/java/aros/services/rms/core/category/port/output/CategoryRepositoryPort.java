/* (C) 2026 */
package aros.services.rms.core.category.port.output;

import aros.services.rms.core.category.domain.Category;
import java.util.List;
import java.util.Optional;

/**
 * Output port for category persistence operations.
 */
public interface CategoryRepositoryPort {
  Category save(Category category);

  Optional<Category> findById(Long id);

  Optional<Category> findByName(String name);

  List<Category> findAll();

  boolean existsById(Long id);
}