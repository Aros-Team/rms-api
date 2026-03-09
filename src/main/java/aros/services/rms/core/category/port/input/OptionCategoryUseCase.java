/* (C) 2026 */
package aros.services.rms.core.category.port.input;

import aros.services.rms.core.category.domain.OptionCategory;
import java.util.List;

/**
 * Input port for option category management. Option categories define customization types (e.g.,
 * "Cooking term", "Milk type"). These are different from product categories.
 */
public interface OptionCategoryUseCase {

  OptionCategory create(OptionCategory optionCategory);

  OptionCategory update(Long id, OptionCategory optionCategory);

  List<OptionCategory> findAll();

  OptionCategory findById(Long id);
}
