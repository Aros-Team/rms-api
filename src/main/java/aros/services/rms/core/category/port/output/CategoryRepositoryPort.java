/* (C) 2026 */

package aros.services.rms.core.category.port.output;

import aros.services.rms.core.category.domain.Category;
import java.util.List;
import java.util.Optional;

/** Output port for category persistence operations. */
public interface CategoryRepositoryPort {
  /**
   * Saves a category to the repository.
   *
   * @param category the category to save
   * @return the saved category with generated ID
   */
  Category save(Category category);

  /**
   * Finds a category by its identifier.
   *
   * @param id the category identifier
   * @return Optional containing the category if found
   */
  Optional<Category> findById(Long id);

  /**
   * Finds a category by its name.
   *
   * @param name the category name to search
   * @return Optional containing the category if found
   */
  Optional<Category> findByName(String name);

  /**
   * Retrieves all categories.
   *
   * @return list of all categories
   */
  List<Category> findAll();

  /**
   * Checks if a category exists by its identifier.
   *
   * @param id the category identifier to check
   * @return true if category exists
   */
  boolean existsById(Long id);
}
