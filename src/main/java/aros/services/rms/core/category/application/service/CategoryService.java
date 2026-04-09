/* (C) 2026 */
package aros.services.rms.core.category.application.service;

import aros.services.rms.core.category.application.exception.CategoryNotFoundException;
import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.port.input.CategoryUseCase;
import aros.services.rms.core.category.port.output.CategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementation of product category management use cases. Handles CRUD operations and
 * enable/disable logic for product categories.
 */
public class CategoryService implements CategoryUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(CategoryService.class);
  private final CategoryRepositoryPort categoryRepositoryPort;
  private final Logger logger;

  public CategoryService(CategoryRepositoryPort categoryRepositoryPort, Logger logger) {
    this.categoryRepositoryPort = categoryRepositoryPort;
    this.logger = logger;
  }

  /** {@inheritDoc} Creates a new product category with enabled status by default. */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Category create(Category category) {
    category.setEnabled(true);
    Category saved = categoryRepositoryPort.save(category);
    logger.info("Category created: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  @Recover
  public Category recoverCreate(DataAccessException e, Category category) {
    log.warn(
        "BD no disponible - fallback para create(category={}): {}",
        category.getName(),
        e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /** {@inheritDoc} Updates name and description of an existing category. */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Category update(Long id, Category category) {
    Category existing =
        categoryRepositoryPort.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));

    existing.setName(category.getName());
    existing.setDescription(category.getDescription());

    Category saved = categoryRepositoryPort.save(existing);
    logger.info("Category updated: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  @Recover
  public Category recoverUpdate(DataAccessException e, Long id, Category category) {
    log.warn("BD no disponible - fallback para update(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /** {@inheritDoc} */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public List<Category> findAll() {
    return categoryRepositoryPort.findAll();
  }

  @Recover
  public List<Category> recoverFindAll(DataAccessException e) {
    log.warn("BD no disponible - fallback para findAll: {}", e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /** {@inheritDoc} */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Category findById(Long id) {
    return categoryRepositoryPort.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
  }

  @Recover
  public Category recoverFindById(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para findById(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /** {@inheritDoc} Toggles the enabled flag of a product category. */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public Category toggleEnabled(Long id) {
    Category existing =
        categoryRepositoryPort.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));

    existing.setEnabled(!existing.isEnabled());

    Category saved = categoryRepositoryPort.save(existing);
    logger.info("Category toggled: id={}, enabled={}", saved.getId(), saved.isEnabled());
    return saved;
  }

  @Recover
  public Category recoverToggleEnabled(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para toggleEnabled(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
