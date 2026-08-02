/* (C) 2026 */

package aros.services.rms.core.category.application.service;

import aros.services.rms.core.category.application.exception.OptionGroupNotFoundException;
import aros.services.rms.core.category.domain.OptionGroup;
import aros.services.rms.core.category.port.input.OptionGroupUseCase;
import aros.services.rms.core.category.port.output.OptionGroupRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.infraestructure.common.exception.ServiceUnavailableException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * Implementation of option category management use cases. Handles CRUD for customization categories
 * (e.g., "Cooking term", "Milk type").
 */
public class OptionGroupService implements OptionGroupUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(OptionGroupService.class);
  private final OptionGroupRepositoryPort optionGroupRepositoryPort;
  private final Logger logger;

  /**
   * Creates a new OptionGroupService instance.
   *
   * @param optionGroupRepositoryPort the option category repository port
   * @param logger the logger instance
   */
  public OptionGroupService(OptionGroupRepositoryPort optionGroupRepositoryPort, Logger logger) {
    this.optionGroupRepositoryPort = optionGroupRepositoryPort;
    this.logger = logger;
  }

  /**
   * Creates a new option category.
   *
   * @param optionGroup the option category data to create
   * @return the created option category with generated ID
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public OptionGroup create(OptionGroup optionGroup) {
    OptionGroup saved = optionGroupRepositoryPort.save(optionGroup);
    logger.info("OptionGroup created: id={}, name={}", saved.getId(), saved.getName());
    return saved;
  }

  /**
   * Updates an existing option category.
   *
   * @param id the option category identifier
   * @param optionGroup the option category data with updates
   * @return the updated option category
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public OptionGroup update(Long id, OptionGroup optionGroup) {
    OptionGroup existing =
        optionGroupRepositoryPort
            .findById(id)
            .orElseThrow(() -> new OptionGroupNotFoundException(id));

    existing.setName(optionGroup.getName());
    existing.setDescription(optionGroup.getDescription());
    existing.setSelectionType(optionGroup.getSelectionType());
    existing.setReplaceSupplyCategoryId(optionGroup.getReplaceSupplyCategoryId());

    OptionGroup saved = optionGroupRepositoryPort.save(existing);
    logger.info("OptionGroup updated: id={}, name={}", saved.getId(), saved.getName());
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
  public List<OptionGroup> findAll() {
    return optionGroupRepositoryPort.findAll();
  }

  /** {@inheritDoc} */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public List<OptionGroup> findByNameContainingIgnoreCase(String name) {
    return optionGroupRepositoryPort.findByNameContainingIgnoreCase(name);
  }

  /** {@inheritDoc} */
  @Override
  public Map<Long, String> loadSelectionTypesByIds(Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return Map.of();
    }
    return optionGroupRepositoryPort.loadSelectionTypesByIds(ids);
  }

  /**
   * Finds an option category by its identifier.
   *
   * @param id the option category identifier
   * @return the found option category
   * @throws OptionGroupNotFoundException if not found
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public OptionGroup findById(Long id) {
    return optionGroupRepositoryPort
        .findById(id)
        .orElseThrow(() -> new OptionGroupNotFoundException(id));
  }

  /**
   * Recovery handler for create operation when database is unavailable.
   *
   * @param e the data access exception
   * @param optionGroup the option category that was being created
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public OptionGroup recoverCreate(DataAccessException e, OptionGroup optionGroup) {
    log.warn(
        "BD no disponible - fallback para create(optionGroup={}): {}",
        optionGroup.getName(),
        e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Recovery handler for update operation when database is unavailable.
   *
   * @param e the data access exception
   * @param id the option category identifier being updated
   * @param optionGroup the option category data with updates
   * @return never returns, always throws ServiceUnavailableException
   * @throws ServiceUnavailableException when database is unavailable
   */
  @Recover
  public OptionGroup recoverUpdate(DataAccessException e, Long id, OptionGroup optionGroup) {
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
  public List<OptionGroup> recoverFindAll(DataAccessException e) {
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
  public OptionGroup recoverFindById(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para findById(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
