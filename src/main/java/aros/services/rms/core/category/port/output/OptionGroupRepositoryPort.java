/* (C) 2026 */

package aros.services.rms.core.category.port.output;

import aros.services.rms.core.category.domain.OptionGroup;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Output port for option category persistence operations. */
public interface OptionGroupRepositoryPort {
  /**
   * Saves an option category to the repository.
   *
   * @param optionGroup the option category to save
   * @return the saved option category with generated ID
   */
  OptionGroup save(OptionGroup optionGroup);

  /**
   * Finds an option category by its identifier.
   *
   * @param id the option category identifier
   * @return Optional containing the option category if found
   */
  Optional<OptionGroup> findById(Long id);

  /**
   * Finds option categories whose name contains the given string (case-insensitive).
   *
   * @param name the partial name to search for
   * @return list of matching option categories
   */
  List<OptionGroup> findByNameContainingIgnoreCase(String name);

  /**
   * Retrieves all option categories.
   *
   * @return list of all option categories
   */
  List<OptionGroup> findAll();

  /**
   * Loads selection type projections for option categories.
   *
   * @param ids category identifiers
   * @return selection type keyed by category identifier
   */
  Map<Long, String> loadSelectionTypesByIds(Collection<Long> ids);

  /**
   * Checks if an option category exists by its identifier.
   *
   * @param id the option category identifier to check
   * @return true if option category exists
   */
  boolean existsById(Long id);
}
