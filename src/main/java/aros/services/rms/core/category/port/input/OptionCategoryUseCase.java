/* (C) 2026 */

package aros.services.rms.core.category.port.input;

import aros.services.rms.core.category.domain.OptionCategory;
import java.util.List;

/**
 * Input port for option category management. Option categories define customization types (e.g.,
 * "Cooking term", "Milk type"). These are different from product categories.
 */
public interface OptionCategoryUseCase {

  /**
   * Creates a new option category.
   *
   * @param optionCategory the option category data to create
   * @return the created option category with generated ID
   */
  OptionCategory create(OptionCategory optionCategory);

  /**
   * Updates an existing option category.
   *
   * @param id the option category identifier
   * @param optionCategory the option category data with updates
   * @return the updated option category
   */
  OptionCategory update(Long id, OptionCategory optionCategory);

  /**
   * Retrieves all option categories.
   *
   * @return list of all option categories
   */
  List<OptionCategory> findAll();

  /**
   * Finds an option category by its identifier.
   *
   * @param id the option category identifier
   * @return the found option category
   */
  OptionCategory findById(Long id);
}
