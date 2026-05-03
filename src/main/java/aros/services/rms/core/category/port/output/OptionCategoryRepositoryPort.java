/* (C) 2026 */

package aros.services.rms.core.category.port.output;

import aros.services.rms.core.category.domain.OptionCategory;
import java.util.List;
import java.util.Optional;

/** Output port for option category persistence operations. */
public interface OptionCategoryRepositoryPort {
  /**
   * Saves an option category to the repository.
   *
   * @param optionCategory the option category to save
   * @return the saved option category with generated ID
   */
  OptionCategory save(OptionCategory optionCategory);

  /**
   * Finds an option category by its identifier.
   *
   * @param id the option category identifier
   * @return Optional containing the option category if found
   */
  Optional<OptionCategory> findById(Long id);

  /**
   * Retrieves all option categories.
   *
   * @return list of all option categories
   */
  List<OptionCategory> findAll();

  /**
   * Checks if an option category exists by its identifier.
   *
   * @param id the option category identifier to check
   * @return true if option category exists
   */
  boolean existsById(Long id);
}
