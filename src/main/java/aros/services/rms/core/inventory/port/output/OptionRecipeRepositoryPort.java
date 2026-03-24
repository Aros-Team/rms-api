/* (C) 2026 */
package aros.services.rms.core.inventory.port.output;

import aros.services.rms.core.inventory.domain.OptionRecipe;
import java.util.List;

/** Output port for option recipe persistence operations. */
public interface OptionRecipeRepositoryPort {

  List<OptionRecipe> saveAll(List<OptionRecipe> recipes);

  List<OptionRecipe> findByOptionId(Long optionId);

  List<OptionRecipe> findByOptionIdIn(List<Long> optionIds);

  void deleteByOptionId(Long optionId);
}
