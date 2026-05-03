/* (C) 2026 */

package aros.services.rms.core.category.application.service;

import aros.services.rms.core.category.application.exception.OptionCategoryNotFoundException;
import aros.services.rms.core.category.domain.OptionCategory;
import aros.services.rms.core.category.port.input.OptionCategoryUseCase;
import aros.services.rms.core.category.port.output.OptionCategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementation of option category management use cases. Handles CRUD for customization categories
 * (e.g., "Cooking term", "Milk type").
 */
public class OptionCategoryService implements OptionCategoryUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(OptionCategoryService.class);
  private final OptionCategoryRepositoryPort optionCategoryRepositoryPort;
  private final Logger logger;

  /**
   * Creates a new OptionCategoryService instance.
   *
   * @param optionCategoryRepositoryPort the option category repository port
   * @param logger the logger instance
   */
  public OptionCategoryService(
      OptionCategoryRepositoryPort optionCategoryRepositoryPort, Logger logger) {
    this.optionCategoryRepositoryPort = optionCategoryRepositoryPort;
    this.logger = logger;
  }

  /**
   * Creates a new option category.
   *
   * @param optionCategory the option category data to create
   * @return the created option category with generated ID
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public OptionCategory create(OptionCategory optionCategory) {
    OptionCategory saved = optionCategoryRepositoryPort.save(optionCategory);
    logger.info("OptionCategory created: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  /**
   * Updates an existing option category.
   *
   * @param id the option category identifier
   * @param optionCategory the option category data with updates
   * @return the updated option category
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
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

  /**
   * Retrieves all option categories.
   *
   * @return list of all option categories
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public List<OptionCategory> findAll() {
    return optionCategoryRepositoryPort.findAll();
  }

  /**
   * Finds an option category by its identifier.
   *
   * @param id the option category identifier
   * @return the found option category
   * @throws OptionCategoryNotFoundException if not found
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public OptionCategory findById(Long id) {
    return optionCategoryRepositoryPort
        .findById(id)
        .orElseThrow(() -> new OptionCategoryNotFoundException(id));
  }

  /**
   * Recovery handler for create operation when database is unavailable.
   *
   * @param e the data access exception
   * @param optionCategory the option category that was being created
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public OptionCategory recoverCreate(DataAccessException e, OptionCategory optionCategory) {
    log.warn(
        "BD no disponible - fallback para create(optionCategory={}): {}",
        optionCategory.getName(),
        e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Recovery handler for update operation when database is unavailable.
   *
   * @param e the data access exception
   * @param id the option category identifier being updated
   * @param optionCategory the option category data with updates
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public OptionCategory recoverUpdate(
      DataAccessException e, Long id, OptionCategory optionCategory) {
    log.warn("BD no disponible - fallback para update(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Recovery handler for findAll operation when database is unavailable.
   *
   * @param e the data access exception
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public List<OptionCategory> recoverFindAll(DataAccessException e) {
    log.warn("BD no disponible - fallback para findAll: {}", e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Recovery handler for findById operation when database is unavailable.
   *
   * @param e the data access exception
   * @param id the option category identifier being looked up
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public OptionCategory recoverFindById(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para findById(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
