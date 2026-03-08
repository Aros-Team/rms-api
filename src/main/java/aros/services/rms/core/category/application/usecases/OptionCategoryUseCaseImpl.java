/* (C) 2026 */
package aros.services.rms.core.category.application.usecases;

import aros.services.rms.core.category.application.exception.OptionCategoryNotFoundException;
import aros.services.rms.core.category.domain.OptionCategory;
import aros.services.rms.core.category.port.input.OptionCategoryUseCase;
import aros.services.rms.core.category.port.output.OptionCategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import java.util.List;

/**
 * Implementation of option category management use cases. Handles CRUD for customization categories
 * (e.g., "Cooking term", "Milk type").
 */
public class OptionCategoryUseCaseImpl implements OptionCategoryUseCase {

  private final OptionCategoryRepositoryPort optionCategoryRepositoryPort;
  private final Logger logger;

  public OptionCategoryUseCaseImpl(
      OptionCategoryRepositoryPort optionCategoryRepositoryPort, Logger logger) {
    this.optionCategoryRepositoryPort = optionCategoryRepositoryPort;
    this.logger = logger;
  }

  @Override
  public OptionCategory create(OptionCategory optionCategory) {
    OptionCategory saved = optionCategoryRepositoryPort.save(optionCategory);
    logger.info("OptionCategory created: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  @Override
  public OptionCategory update(Long id, OptionCategory optionCategory) {
    OptionCategory existing =
        optionCategoryRepositoryPort
            .findById(id)
            .orElseThrow(() -> new OptionCategoryNotFoundException(id));

    existing.setName(optionCategory.getName());
    existing.setDescription(optionCategory.getDescription());

    OptionCategory saved = optionCategoryRepositoryPort.save(existing);
    logger.info("OptionCategory updated: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  @Override
  public List<OptionCategory> findAll() {
    return optionCategoryRepositoryPort.findAll();
  }

  @Override
  public OptionCategory findById(Long id) {
    return optionCategoryRepositoryPort
        .findById(id)
        .orElseThrow(() -> new OptionCategoryNotFoundException(id));
  }
}