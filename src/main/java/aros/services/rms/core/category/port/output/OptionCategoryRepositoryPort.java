/* (C) 2026 */
package aros.services.rms.core.category.port.output;

import aros.services.rms.core.category.domain.OptionCategory;
import java.util.List;
import java.util.Optional;

/** Output port for option category persistence operations. */
public interface OptionCategoryRepositoryPort {
  OptionCategory save(OptionCategory optionCategory);

  Optional<OptionCategory> findById(Long id);

  List<OptionCategory> findAll();

  boolean existsById(Long id);
}
