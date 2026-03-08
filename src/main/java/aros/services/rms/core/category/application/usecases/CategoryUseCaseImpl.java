/* (C) 2026 */
package aros.services.rms.core.category.application.usecases;

import aros.services.rms.core.category.application.exception.CategoryNotFoundException;
import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.port.input.CategoryUseCase;
import aros.services.rms.core.category.port.output.CategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import java.util.List;

/**
 * Implementation of product category management use cases. Handles CRUD operations and
 * enable/disable logic for product categories.
 */
public class CategoryUseCaseImpl implements CategoryUseCase {

  private final CategoryRepositoryPort categoryRepositoryPort;
  private final Logger logger;

  public CategoryUseCaseImpl(CategoryRepositoryPort categoryRepositoryPort, Logger logger) {
    this.categoryRepositoryPort = categoryRepositoryPort;
    this.logger = logger;
  }

  /** {@inheritDoc} Creates a new product category with enabled status by default. */
  @Override
  public Category create(Category category) {
    category.setEnabled(true);
    Category saved = categoryRepositoryPort.save(category);
    logger.info("Category created: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  /** {@inheritDoc} Updates name and description of an existing category. */
  @Override
  public Category update(Long id, Category category) {
    Category existing =
        categoryRepositoryPort
            .findById(id)
            .orElseThrow(() -> new CategoryNotFoundException(id));

    existing.setName(category.getName());
    existing.setDescription(category.getDescription());

    Category saved = categoryRepositoryPort.save(existing);
    logger.info("Category updated: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  /** {@inheritDoc} */
  @Override
  public List<Category> findAll() {
    return categoryRepositoryPort.findAll();
  }

  /** {@inheritDoc} */
  @Override
  public Category findById(Long id) {
    return categoryRepositoryPort
        .findById(id)
        .orElseThrow(() -> new CategoryNotFoundException(id));
  }

  /** {@inheritDoc} Toggles the enabled flag of a product category. */
  @Override
  public Category toggleEnabled(Long id) {
    Category existing =
        categoryRepositoryPort
            .findById(id)
            .orElseThrow(() -> new CategoryNotFoundException(id));

    existing.setEnabled(!existing.isEnabled());

    Category saved = categoryRepositoryPort.save(existing);
    logger.info("Category toggled: id={}, enabled={}", saved.getId(), saved.isEnabled());
    return saved;
  }
}