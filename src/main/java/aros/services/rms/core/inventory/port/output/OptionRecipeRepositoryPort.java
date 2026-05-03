/* (C) 2026 */

package aros.services.rms.core.inventory.port.output;

import aros.services.rms.core.inventory.domain.OptionRecipe;
import java.util.List;

/** Output port for option recipe persistence operations. */
public interface OptionRecipeRepositoryPort {

  /**
   * Saves multiple option recipes.
   *
   * @param recipes the recipes to save
   * @return saved recipes
   */
  List<OptionRecipe> saveAll(List<OptionRecipe> recipes);

  /**
   * Finds recipes by option ID.
   *
   * @param optionId the option ID
   * @return list of recipes
   */
  List<OptionRecipe> findByOptionId(Long optionId);

  /**
   * Finds recipes by multiple option IDs.
   *
   * @param optionIds list of option IDs
   * @return list of recipes
   */
  List<OptionRecipe> findByOptionIdIn(List<Long> optionIds);

  /**
   * Deletes recipes by option ID.
   *
   * @param optionId the option ID
   */
  void deleteByOptionId(Long optionId);
}
