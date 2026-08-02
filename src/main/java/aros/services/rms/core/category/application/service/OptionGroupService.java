/* (C) 2026 */

package aros.services.rms.core.category.application.service;

import aros.services.rms.core.category.application.exception.OptionGroupNotFoundException;
import aros.services.rms.core.category.application.exception.OptionGroupRequiresProductException;
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
 * Implementation of option group management use cases. Handles CRUD for customization groups (e.g.,
 * "Proteína Hamburguesa", "Acompañamiento Parrilla") and enforces the business rule that every
 * option group must be attached to at least one product.
 */
public class OptionGroupService implements OptionGroupUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(OptionGroupService.class);
  private final OptionGroupRepositoryPort optionGroupRepositoryPort;
  private final Logger logger;

  /**
   * Creates a new OptionGroupService instance.
   *
   * @param optionGroupRepositoryPort the option group repository port
   * @param logger the logger instance
   */
  public OptionGroupService(OptionGroupRepositoryPort optionGroupRepositoryPort, Logger logger) {
    this.optionGroupRepositoryPort = optionGroupRepositoryPort;
    this.logger = logger;
  }

  /**
   * Creates a new option group, enforcing that at least one product ID is supplied.
   *
   * @param optionGroup the option group data to create
   * @param productIds the IDs of the products this group applies to (must contain at least one)
   * @param required the required flag applied to every product association in this group
   * @return the created option group with generated ID
   * @throws OptionGroupRequiresProductException if {@code productIds} is empty
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public OptionGroup create(OptionGroup optionGroup, List<Long> productIds, boolean required) {
    if (productIds == null || productIds.isEmpty()) {
      throw new OptionGroupRequiresProductException();
    }
    OptionGroup saved = optionGroupRepositoryPort.save(optionGroup);
    optionGroupRepositoryPort.replaceProductAssociations(saved.getId(), productIds, required);
    logger.info(
        "OptionGroup created: id={}, name={}, products={}",
        saved.getId(),
        saved.getName(),
        productIds);
    return saved;
  }

  /**
   * Updates an existing option group and replaces its product associations.
   *
   * @param id the option group identifier
   * @param optionGroup the option group data with updates
   * @param productIds the IDs of the products this group applies to (must contain at least one)
   * @param required the required flag applied to every product association in this group
   * @return the updated option group
   * @throws OptionGroupRequiresProductException if {@code productIds} is empty
   */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public OptionGroup update(
      Long id, OptionGroup optionGroup, List<Long> productIds, boolean required) {
    if (productIds == null || productIds.isEmpty()) {
      throw new OptionGroupRequiresProductException();
    }
    OptionGroup existing =
        optionGroupRepositoryPort
            .findById(id)
            .orElseThrow(() -> new OptionGroupNotFoundException(id));

    existing.setName(optionGroup.getName());
    existing.setDescription(optionGroup.getDescription());
    existing.setSelectionType(optionGroup.getSelectionType());
    existing.setReplaceSupplyCategoryId(optionGroup.getReplaceSupplyCategoryId());

    OptionGroup saved = optionGroupRepositoryPort.save(existing);
    optionGroupRepositoryPort.replaceProductAssociations(saved.getId(), productIds, required);
    logger.info(
        "OptionGroup updated: id={}, name={}, products={}",
        saved.getId(),
        saved.getName(),
        productIds);
    return saved;
  }

  /**
   * Retrieves all option groups.
   *
   * @return list of all option groups
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
   * Finds an option group by its identifier.
   *
   * @param id the option group identifier
   * @return the found option group
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

  /** {@inheritDoc} */
  @Override
  @Retryable(
      retryFor = {DataAccessException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000))
  public List<OptionGroup> findByProductId(Long productId) {
    return optionGroupRepositoryPort.findByProductId(productId);
  }

  /** {@inheritDoc} */
  @Override
  public Map<Long, List<Long>> loadProductIdsByOptionGroupIds(Collection<Long> optionGroupIds) {
    if (optionGroupIds == null || optionGroupIds.isEmpty()) {
      return Map.of();
    }
    return optionGroupRepositoryPort.loadProductIdsByOptionGroupIds(optionGroupIds);
  }

  /**
   * Recovery handler for create operation when database is unavailable.
   *
   * @param e the data access exception
   * @return never returns, always throws ServiceUnavailableException
   */
  @Recover
  public OptionGroup recoverCreate(
      DataAccessException e, OptionGroup optionGroup, List<Long> productIds, boolean required) {
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
   * @return never returns, always throws ServiceUnavailableException
   */
  @Recover
  public OptionGroup recoverUpdate(
      DataAccessException e,
      Long id,
      OptionGroup optionGroup,
      List<Long> productIds,
      boolean required) {
    log.warn("BD no disponible - fallback para update(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Recovery handler for findAll operation when database is unavailable.
   *
   * @param e the data access exception
   * @return never returns, always throws ServiceUnavailableException
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
   * @return never returns, always throws ServiceUnavailableException
   */
  @Recover
  public OptionGroup recoverFindById(DataAccessException e, Long id) {
    log.warn("BD no disponible - fallback para findById(id={}): {}", id, e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Recovery handler for findByNameContainingIgnoreCase when database is unavailable.
   *
   * @param e the data access exception
   * @return never returns, always throws ServiceUnavailableException
   */
  @Recover
  public List<OptionGroup> recoverFindByNameContainingIgnoreCase(
      DataAccessException e, String name) {
    log.warn(
        "BD no disponible - fallback para findByNameContainingIgnoreCase({}): {}",
        name,
        e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }

  /**
   * Recovery handler for findByProductId when database is unavailable.
   *
   * @param e the data access exception
   * @return never returns, always throws ServiceUnavailableException
   */
  @Recover
  public List<OptionGroup> recoverFindByProductId(DataAccessException e, Long productId) {
    log.warn(
        "BD no disponible - fallback para findByProductId(productId={}): {}",
        productId,
        e.getMessage());
    throw new ServiceUnavailableException("Servicio temporalmente no disponible");
  }
}
